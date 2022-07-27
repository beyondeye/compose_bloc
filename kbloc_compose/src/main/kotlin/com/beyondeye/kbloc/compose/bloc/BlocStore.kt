package com.beyondeye.kbloc.compose.model

import androidx.compose.runtime.*
import com.beyondeye.kbloc.compose.navigator.Navigator
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.core.BlocBase
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private typealias BlocKey = String

public class BlocStore {
    @PublishedApi
    internal val blocs_mutex = Mutex()
    @PublishedApi
    internal var blocs: PersistentMap<BlocKey, BlocBase<*>> = persistentHashMapOf()
    @PublishedApi
    internal val blocs_dependencies_mutex = Mutex()
    @PublishedApi
    internal var blocs_dependencies:PersistentMap<DependencyKey,Dependency> = persistentHashMapOf()


    /**
     * key for a [BlocBase] of a specific type T for a specific [screen], with an additional
     * optional user defined [tag] appended in the end, in case there are more than one [BlocBase]
     * of the same type for this [Screen]
     */
    @PublishedApi
    internal inline fun <reified T : BlocBase<*>> getKey(screen: Screen, tag: String?): BlocKey =
        "${screen.key}:${T::class.qualifiedName}:${tag ?: "default"}"

    @PublishedApi
    internal fun getDependencyKey(bloc: BlocBase<*>, name: String): DependencyKey =
        blocs
            .firstNotNullOfOrNull {
                if (it.value == bloc) it.key
                else null
            }
            ?: ScreenModelStore.lastScreenModelKey.value
                ?.let { "$it:$name" }
            ?: "standalone:$name"


    @PublishedApi
    internal inline fun <reified T : BlocBase<*>> getOrPut(
        screen: Screen,
        tag: String?,
        crossinline factory: @DisallowComposableCalls () -> T
    ): BlocBase<*> {
        val key = getKey<T>(screen, tag)
        var b = blocs.get(key)
        if (b != null) return b
        b = factory()
        runBlocking {
            blocs_mutex.withLock {
                blocs = blocs.put(key, b)
            }
        }
        return b
    }

    @PublishedApi
    internal inline fun <reified T : Any> getOrPutDependency(
        bloc: BlocBase<*>,
        name: String,
        noinline onDispose: @DisallowComposableCalls (T) -> Unit = {},
        noinline factory: @DisallowComposableCalls (DependencyKey) -> T
    ): T {
        val key = getDependencyKey(bloc, name)

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
                blocs[key]?.close() //onDispose()
                blocs_mutex.withLock {
                    blocs = blocs.remove(key)
                }
            }
        }
        //TODO use instead [onEach] as in original ScreenModelStore code?
        val dep_keys_to_remove = blocs_dependencies.extractKeyAssociatedToScreen(screen)
        //todo instead of runBlocking run it in a separate thread?
        runBlocking {
            for (key in dep_keys_to_remove) {
                blocs_dependencies[key]?.let { (instance, onDispose) -> onDispose(instance) }
                blocs_dependencies_mutex.withLock {
                    blocs_dependencies = blocs_dependencies.remove(key)
                }
            }
        }
    }
    private fun Map<String, *>.extractKeyAssociatedToScreen(screen: Screen) =
            mapNotNull{
                val k = it.key
                if (k.startsWith(screen.key)) k else null
            }
}

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
val LocalBlocStore = compositionLocalOf(policy = referentialEqualityPolicy()) { BlocStore() }

@Composable
public inline fun <reified T : BlocBase<*>> Screen.rememberBloc(
    tag: String? = null,
    crossinline factory: @DisallowComposableCalls () -> T
): T {
    val store= LocalBlocStore.current
    val res=remember(store.getKey<T>(this, tag)) {
        store.getOrPut(this, tag, factory)
    }
    return res as T
}

//TODO do we need this method or not?
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