package com.beyondeye.kbloc.compose.model

import androidx.compose.runtime.*
import com.beyondeye.kbloc.compose.navigator.Navigator
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.core.BlocBase
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private typealias BlocKey = String

//-----------------------------------------------------------
object BlocStore {
    @PublishedApi
    /**
     *  multiplatform persistent map: see https://github.com/Kotlin/kotlinx.collections.immutable
     *  it is also used under the hood in androix.compose.runtime library for implementation of
     *  CompositionLocalMap used for storing [CompositionLocal] data
     */
    internal var blocs: PersistentMap<BlocKey, BlocBase<*>> = persistentHashMapOf()

    @PublishedApi
    internal var blocs_dependencies: PersistentMap<DependencyKey, Dependency> =
        persistentHashMapOf()

    @PublishedApi
    internal val blocs_mutex = Mutex()

    @PublishedApi
    internal val blocs_dependencies_mutex = Mutex()

    /**
     * since we use [PersistentMap] we don't need to duplicate [blocs] and [blocs_dependencies] fields
     */
    //public fun copy() = BlocStore(this.blocs,this.blocs_dependencies)


    /**
     * key for a [BlocBase] of a specific type T for a specific [screen], with an additional
     * optional user defined [blocTag] appended in the end, in case there are more than one [BlocBase]
     * of the same type for this [Screen]
     */
    @PublishedApi
    internal inline fun <reified T : BlocBase<*>> getBlocKey(
        screen: Screen,
        blocTag: String?
    ): BlocKey =
        "${screen.key}:${T::class.qualifiedName}:${blocTag ?: "default"}"

    /**
     * define key for a bloc of a specific type T NOT BOUND to a specific screen, with an additional
     * optional user defined [tag] appended in the end, in case there are more than one [BlocBase] of that type
     * the meaning of a bloc being unbound is that the lifecycle of the bloc will be managed elsewhere. (i.e. some other specific screen)
     */
    @PublishedApi
    internal inline fun <reified T : BlocBase<*>> getBlocKeyForUnboundBloc(tag: String?): BlocKey =
        "__unbound:${T::class.qualifiedName}:${tag ?: "default"}"

    @PublishedApi
    internal fun getBlocDependencyKey(bloc: BlocBase<*>, name: String): DependencyKey =
        blocs
            .firstNotNullOfOrNull {
                if (it.value == bloc) it.key
                else null
            }
            ?: ScreenModelStore.lastScreenModelKey.value
                ?.let { "$it:$name" }
            ?: "standalone:$name"

    /**
     * same as method above but instead of searching for the bloc instance in the [blocs] store
     * directly generate the bloc key with the same parameters that are used in [getBlocKey]
     * that is [screen] and [blocTag]
     */
    @PublishedApi
    internal inline fun <reified T : BlocBase<*>> getBlocDependencyKey(
        screen: Screen,
        blocTag: String?,
        name: String
    ): DependencyKey {
        val blocKey = getBlocKey<T>(screen, blocTag)
        return "$blocKey:$name"
    }


    //----------------------------------------------------------------
    @PublishedApi
    internal inline fun <reified T : BlocBase<*>> getOrPut(
        key:BlocKey,
        cscope: CoroutineScope,
        crossinline factory: @DisallowComposableCalls (cscope: CoroutineScope) -> T
    ): BlocBase<*> {
//        val key = getBlocKey<T>(screen, tag)
        var b = blocs.get(key)
        if (b != null) return b
        b = factory(cscope)
        runBlocking {
            blocs_mutex.withLock {
                blocs = blocs.put(key, b)
            }
        }
        return b
    }

    /**
     * put an UNBOUND bloc in the bloc store: an unbound bloc is a bloc not associated to a screen
     * return the key
     */
    @PublishedApi
    internal inline fun <reified T : BlocBase<*>> putUnbound(
        bloc: T,
        tag: String?,
    ): String {
        val key = getBlocKeyForUnboundBloc<T>(tag)
        runBlocking {
            blocs_mutex.withLock {
                blocs = blocs.put(key, bloc)
            }
        }
        return key
    }

    /**
     * remove an UNBOUND bloc from the bloc store: an unbound bloc is a bloc not associated to a screen
     * return the key
     */
    @PublishedApi
    internal inline fun <reified T : BlocBase<*>> removeUnbound(
        tag: String?,
    ) {
        val key = getBlocKeyForUnboundBloc<T>(tag)
        runBlocking {
            blocs_mutex.withLock {
                blocs = blocs.remove(key)
            }
        }
    }

    /**
     * put unbound and automatically remove it when the composable exit the composition
     */
    @Composable
    internal inline fun <reified T : BlocBase<*>> putUnboundWithAutoRemove(
        bloc: T,
        tag: String?,
    ) {
        DisposableEffect(true) {
            val key = putUnbound(bloc, tag)
            onDispose {
                runBlocking {
                    blocs_mutex.withLock {
                        blocs = blocs.remove(key)
                    }
                }
            }
        }
    }


    @PublishedApi
    internal inline fun <reified T : Any> getOrPutDependency(
        bloc: BlocBase<*>,
        name: String,
        noinline onDispose: @DisallowComposableCalls (T) -> Unit = {},
        noinline factory: @DisallowComposableCalls (DependencyKey) -> T
    ): T {
        val key = getBlocDependencyKey(bloc, name)

        var dep = blocs_dependencies.get(key)
        if (dep != null) return dep as T
        dep = factory(key) as Dependency
        runBlocking {
            blocs_dependencies_mutex.withLock {
                blocs_dependencies = blocs_dependencies.put(key, dep)
            }
        }
        return dep as T
    }


    /**
     * same as method above but instead of passing a bloc instance, pass the directly the
     * parameters needed for defining the block dependency key
     */
    @PublishedApi
    internal inline fun <reified T : Any, reified B : BlocBase<*>> getOrPutDependency(
        screen: Screen,
        blockTag: String?,
        name: String,
        noinline onDispose: @DisallowComposableCalls (T) -> Unit = {},
        noinline factory: @DisallowComposableCalls (DependencyKey) -> T
    ): T {
        val key = getBlocDependencyKey<B>(screen, blockTag, name)

        var dep = blocs_dependencies.get(key)
        if (dep != null) return dep as T
        dep = factory(key) as Dependency
        runBlocking {
            blocs_dependencies_mutex.withLock {
                blocs_dependencies = blocs_dependencies.put(key, dep)
            }
        }
        return dep as T
    }


    /**
     * method that is called from [Navigator.dispose] when a [Screen] is popped from the navigation
     * stack
     */
    public fun remove(screen: Screen) {
        //TODO use instead [onEach] as in original ScreenModelStore code?
        val bloc_keys_to_remove = blocs.extractKeyAssociatedToScreen(screen)
        //todo instead of runBlocking run it in a separate thread?
        runBlocking {
            for (key in bloc_keys_to_remove) {
                blocs[key]?.let { b ->
                    b.close()
                    b.cscope.cancel()
                }
            }
            blocs_mutex.withLock {
                blocs = blocs.mutate {
                    for (key in bloc_keys_to_remove) {
                        it.remove(key)
                    }
                }
            }
        }
        //TODO use instead [onEach] as in original ScreenModelStore code?
        val dep_keys_to_remove = blocs_dependencies.extractKeyAssociatedToScreen(screen)
        if (dep_keys_to_remove.size > 0) {
            //todo instead of runBlocking run it in a separate thread?
            runBlocking {
                for (key in dep_keys_to_remove) {
                    blocs_dependencies[key]?.let { (instance, onDispose) -> onDispose(instance) }
                }
                blocs_dependencies_mutex.withLock {
                    blocs_dependencies = blocs_dependencies.mutate {
                        for (key in dep_keys_to_remove) {
                            it.remove(key)
                        }
                    }
                }
            }
        }
    }

    private fun Map<String, *>.extractKeyAssociatedToScreen(screen: Screen) =
        mapNotNull {
            val k = it.key
            if (k.startsWith(screen.key)) k else null
        }
}

//-----------------------------------------------------------
/**
 * the current set of active blocs is scoped to the current composition tree node
 */
//TODO is referentialEqualityPolicy() correct here? I think so since we already listen
// see also https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#referentialEqualityPolicy()
//   separately to changes to the bloc state to trigger recomposition, so no need to
//   listen to other things. Only if the actual instance of the bloc change then we should
//   probably retrigger composition in addition to the state change trigger. need to think about this
//TODO: should I use here staticCompositionLocalOf? the compose docs are not very clear about it
// see https://developer.android.com/jetpack/compose/compositionlocal#creating-apis
//val LocalBlocStore = compositionLocalOf(policy = referentialEqualityPolicy()) { BlocStore() }


/**
 *
 */
//TODO remove this method since according to the code in Navigator.kt, using CompositionLocalProvider
// require the data stored in to be "saveable": see the code there
@Composable
public fun DefineNewBlocStoreForSubTree(content_subtree: @Composable () -> Unit) {
    /*
    //todo need to use remembersavable for the current localBlocStore here, but how can I serialize
    // blocs?
    // look at Navigator.kt
    // look also at this discussion: https://github.com/adrielcafe/voyager/discussions/4
    val newstore= LocalBlocStore.current.copy() //TODO: user remember here?
    CompositionLocalProvider(LocalBlocStore provides newstore, content = content_subtree)
     */
}
//-----------------------------------------------------------
/**
 * @param factory is a builder method for the [Bloc] or [Cubit], it takes a [CoroutineScope]
 * parameter because it is needed for building any [Bloc]
 */
@Composable
public inline fun <reified T : BlocBase<*>> Screen.rememberBloc(
    tag: String? = null,
    crossinline factory: @DisallowComposableCalls (cscope: CoroutineScope) -> T,
    dispatcher: CoroutineDispatcher? = null
): T {
//    val store= LocalBlocStore.current
    val store = BlocStore
    val bkey = store.getBlocKey<T>(this, tag)
//    val cscope= rememberCoroutineScope()
//    val cscope= blocCoroutineScope<T>(screen = this, blocTag = tag)
    //we currently manually cancel the cscope when the bloc is closed, instead of using rememberCoroutineScope() that cancel it automatically
    // also we don't use blocCoroutineScope because we don't need to store a separate dependency for the scope
    // since we store it inside the bloc itself
    var cscope = MainScope() + CoroutineName(bkey)
    if(dispatcher!=null) cscope +=dispatcher
    return remember(bkey) {
        store.getOrPut(bkey, cscope, factory) as T
    }
}


/*
//-----------------------------------------------------------
//TODO do we need this method or not? or perhaps use the the scope of the screen?
@Composable
public fun BlocBase<*>.coroutineScope(): CoroutineScope {
    val store= LocalBlocStore.current
    return store.getOrPutDependency(
        bloc = this,
        name = "ScreenModelCoroutineScope",
        factory = { key -> MainScope() + CoroutineName(key) }, //MainScope() means Dispatchers.Main, the main thread, CoroutineName is only for debygging
        onDispose = { scope -> scope.cancel() } //cancel all coroutine in this scope when onDispose is called
    )
}

 */

//-----------------------------------------------------------
//TODO do we need this method or not? or perhaps use the the scope of the screen? and avoid defining this
//as a dependency?
@Composable
public inline fun <reified B : BlocBase<*>> blocCoroutineScope(
    screen: Screen,
    blocTag: String?
): CoroutineScope {
    //TODO use instead     //TODO use instead val scope = rememberCoroutineScope()? see https://developer.android.com/jetpack/compose/side-effects#remembercoroutinescope
//    val store= LocalBlocStore.current
    val store = BlocStore
    return store.getOrPutDependency<CoroutineScope, B>(
        screen,
        blocTag,
        name = "ScreenModelCoroutineScope",
        factory = { key -> MainScope() + CoroutineName(key) }, //MainScope() means Dispatchers.Main, the main thread, CoroutineName is only for debygging
        onDispose = { scope -> scope.cancel() } //cancel all coroutine in this scope when onDispose is called
    )
}