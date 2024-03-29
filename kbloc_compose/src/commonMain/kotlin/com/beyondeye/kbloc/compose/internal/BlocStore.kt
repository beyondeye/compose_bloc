package com.beyondeye.kbloc.compose.internal

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
//import com.beyondeye.kbloc.compose.navigator.Navigator
import com.beyondeye.kbloc.core.BlocBase
import com.beyondeye.kbloc.ext.getFullName
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.coroutines.*

internal typealias DependencyKey = String
private typealias DependencyInstance = Any

internal typealias DependencyOnDispose = (Any) -> Unit

internal typealias Dependency = Pair<DependencyInstance, DependencyOnDispose>

private typealias BlocKey = String


//-----------------------------------------------------------
public class BlocStore {
    /**
     *  multiplatform persistent map: see https://github.com/Kotlin/kotlinx.collections.immutable
     *  it is also used under the hood in androix.compose.runtime library for implementation of
     *  CompositionLocalMap used for storing [CompositionLocal] data
     *  this is an atomic reference to persistent map instead of ThreadSafeMap
     *  note that AtomicRef data should not be made public according to atomicfu docs
     */
    @PublishedApi
    internal val blocs: AtomicRef<PersistentMap<BlocKey, BlocBase<*>>> =
        atomic(persistentHashMapOf())
    public val blocs_value:PersistentMap<BlocKey,BlocBase<*>> get() = blocs.value

    //  note that AtomicRef data should not be made public according to atomicfu docs
    @PublishedApi
    internal val blocs_dependencies: AtomicRef<PersistentMap<DependencyKey, Dependency>> =
        atomic(persistentHashMapOf())
    public val blocs_dependencies_value:PersistentMap<DependencyKey, Dependency> get() =
        blocs_dependencies.value

    /**
     * since we use [PersistentMap] we don't need to duplicate [blocs] and [blocs_dependencies] fields
     */
    //public fun copy() = BlocStore(this.blocs,this.blocs_dependencies)


    //----------------------------------------------------------------
    @PublishedApi
    internal inline fun <reified T : BlocBase<*>> getOrPut(
        key: BlocKey,
        cscope: CoroutineScope,
        crossinline factory: @DisallowComposableCalls (cscope: CoroutineScope) -> T
    ): BlocBase<*> {
//        val key = getBlocKey<T>(screen, tag)
        var b = blocs.value.get(key)
        if (b != null) return b
        b = factory(cscope)
        blocs.update { it.put(key, b) }
        return b
    }

//    /**
//     * put an UNBOUND bloc in the bloc store: an unbound bloc is a bloc not associated to a screen
//     * return the key
//     */
//    @PublishedApi
//    internal inline fun <reified T : BlocBase<*>> putUnbound(
//        bloc: T,
//        tag: String?,
//    ): String {
//        val key = getBlocKeyForUnboundBloc<T>(tag)
//        runBlocking {
//            blocs_mutex.withLock {
//                blocs = blocs.put(key, bloc)
//            }
//        }
//        return key
//    }

//    /**
//     * remove an UNBOUND bloc from the bloc store: an unbound bloc is a bloc not associated to a screen
//     * return the key
//     */
//    @PublishedApi
//    internal inline fun <reified T : BlocBase<*>> removeUnbound(
//        tag: String?,
//    ) {
//        val key = getBlocKeyForUnboundBloc<T>(tag)
//        runBlocking {
//            blocs_mutex.withLock {
//                blocs = blocs.remove(key)
//            }
//        }
//    }

//    /**
//     * put unbound and automatically remove it when the composable exit the composition
//     */
//    @Composable
//    internal inline fun <reified T : BlocBase<*>> putUnboundWithAutoRemove(
//        bloc: T,
//        tag: String?,
//    ) {
//        DisposableEffect(true) {
//            val key = putUnbound(bloc, tag)
//            onDispose {
//                runBlocking {
//                    blocs_mutex.withLock {
//                        blocs = blocs.remove(key)
//                    }
//                }
//            }
//        }
//    }


    @PublishedApi
    internal inline fun <reified T : Any> getOrPutDependency(
        bloc: BlocBase<*>,
        name: String,
        //TODO: look how this was used in the original ScreenModelStore.getOrPutDependency()
        noinline onDispose: @DisallowComposableCalls (T) -> Unit = {},
        noinline factory: @DisallowComposableCalls (DependencyKey) -> T
    ): T {
        val key = getBlocDependencyKey(bloc, name)

        var dep = blocs_dependencies.value.get(key)
        if (dep != null) return dep.first as T
        dep = Pair(factory(key) as Any, onDispose as DependencyOnDispose)
        blocs_dependencies.update { it.put(key, dep) }
        return dep.first as T
    }

    @PublishedApi
    internal fun getBlocDependencyKey(bloc: BlocBase<*>, name: String): DependencyKey {
        //TODO perhaps avoid using lastScreenModelKey here so I don't need to access blocStore
        return blocs.value
            .firstNotNullOfOrNull {
                if (it.value == bloc) it.key
                else null
            }
//*DARIO*
// removed use of lastScreenModelKey here so I don't need to access ScreenModelStore
// because now in order to access it I need to make  getBlocDependencyKey composalbe
// In any case I am not currently using Bloc dependency at all
//            ?: ScreenModelStore.lastScreenModelKey.value
            ?.let { "$it:$name" }
            ?: "standalone:$name"

    }
    /**
     * same as method above but instead of searching for the bloc instance in the [blocs] store
     * directly generate the bloc key with the same parameters that are used in [getBlocKey]
     * that is [screen] and [blocTag]
     */

    @PublishedApi
    internal inline fun <reified T : Any, reified B : BlocBase<*>> getOrPutDependency(
        screen: Screen,
        blockTag: String?,
        name: String,
        //TODO: look how this was used in the original ScreenModelStore.getOrPutDependency()
        noinline onDispose: @DisallowComposableCalls (T) -> Unit = {},
        noinline factory: @DisallowComposableCalls (DependencyKey) -> T
    ): T {
        val key = getBlocDependencyKey<B>(screen, blockTag, name)

        var dep = blocs_dependencies.value.get(key)
        if (dep != null) return dep as T
        dep = factory(key) as Dependency

        blocs_dependencies.update { it.put(key, dep) }

        return dep as T
    }


    /**
     * method that is called from [Navigator.dispose] when a [Screen] is popped from the navigation
     * stack
     */
    public fun remove(screen: Screen) {
        //TODO use instead [onEach] as in original ScreenModelStore code?
        val cur_blocs = blocs.value
        val bloc_keys_to_remove = cur_blocs.extractKeyAssociatedToScreen(screen)
        //disposing should not be canceled so run it in GlobalScope
        GlobalScope.async {
            for (key in bloc_keys_to_remove) {
                cur_blocs[key]?.dispose()
            }
        }
        blocs.update {
            it.mutate {
                for (key in bloc_keys_to_remove) {
                    it.remove(key)
                }
            }
        }
        //TODO use instead [onEach] as in original ScreenModelStore code?
        val cur_blocs_deps = blocs_dependencies.value
        val dep_keys_to_remove = cur_blocs_deps.extractKeyAssociatedToScreen(screen)
        if (dep_keys_to_remove.size > 0) {
            for (key in dep_keys_to_remove) {
                cur_blocs_deps[key]?.let { (instance, onDispose) -> onDispose(instance) }
            }
            blocs_dependencies.update {
                it.mutate {
                    for (key in dep_keys_to_remove) {
                        it.remove(key)
                    }
                }
            }
        }
    }

    public companion object {
        /**
         * key for a [BlocBase] of a specific type T for a specific [screen], with an additional
         * optional user defined [blocTag] appended in the end, in case there are more than one [BlocBase]
         * of the same type for this [Screen]
         */
        public inline fun <reified T : BlocBase<*>> getBlocKey(
            screen: Screen,
            blocTag: String?
        ): BlocKey =
            "${screen.key}:${T::class.getFullName()}:${blocTag ?: ""}"

        /**
         * define key for a bloc of a specific type T NOT BOUND to a specific screen, with an additional
         * optional user defined [tag] appended in the end, in case there are more than one [BlocBase] of that type
         * the meaning of a bloc being unbound is that the lifecycle of the bloc will be managed elsewhere. (i.e. some other specific screen)
         */
        @PublishedApi
        internal inline fun <reified T : BlocBase<*>> getBlocKeyForUnboundBloc(tag: String?): BlocKey =
            "__unbound:${T::class.getFullName()}:${tag ?: ""}"

        /**
         * key used internally to store a binding for a certain Bloc type and a certain [bloc_tag]
         * in the composable local content
         */
        @PublishedApi
        internal inline fun <reified B : BlocBase<*>> buildBlocBindingKey(bloc_tag: String?): BlocKey =
            B::class.getFullName() + (bloc_tag ?: "")

        internal fun <B : BlocBase<*>> buildBlocBindingKey(bloc: B, bloc_tag: String?) =
            bloc::class.getFullName() + (bloc_tag ?: "")

        /**
         * same as method above but instead of passing a bloc instance, pass the directly the
         * parameters needed for defining the block dependency key
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

        private fun Map<BlocKey, *>.extractKeyAssociatedToScreen(screen: Screen) =
            mapNotNull {
                val k = it.key
                if (k.startsWith(screen.key)) k else null
            }
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
@PublishedApi
internal inline fun <reified T : BlocBase<*>> Screen.rememberBloc(
    tag: String? = null,
    crossinline factory: @DisallowComposableCalls (cscope: CoroutineScope) -> T,
    dispatcher: CoroutineDispatcher? = null
): Pair<T, String> {
    val store = LocalBlocStoreOwner.current.blocStore
    val bkey = BlocStore.getBlocKey<T>(this, tag)
    //    val cscope= rememberCoroutineScope()
//    val cscope= blocCoroutineScope<T>(screen = this, blocTag = tag)
    //we currently manually cancel the cscope when the bloc is closed, instead of using rememberCoroutineScope() that cancel it automatically
    // also we don't use blocCoroutineScope because we don't need to store a separate dependency for the scope
    // since we store it inside the bloc itself
    var cscope = MainScope() + Dispatchers.Default + CoroutineName(bkey)
    if (dispatcher != null) cscope += dispatcher
    val b = remember(bkey) {
        store.getOrPut(bkey, cscope, factory) as T
    }
    return Pair(b, bkey)
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
    val store = LocalBlocStoreOwner.current.blocStore
    return store.getOrPutDependency<CoroutineScope, B>(
        screen,
        blocTag,
        name = "ScreenModelCoroutineScope",
        factory = { key -> MainScope() + Dispatchers.Default + CoroutineName(key) }, //MainScope() means Dispatchers.Main+SupervisorJob, the main thread, but we change to Dispatchers.Default CoroutineName is only for debygging
        onDispose = { scope -> scope.cancel() } //cancel all coroutine in this scope when onDispose is called
    )
}