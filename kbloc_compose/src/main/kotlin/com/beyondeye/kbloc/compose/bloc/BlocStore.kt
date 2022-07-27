package com.beyondeye.kbloc.compose.model

import androidx.compose.runtime.DisallowComposableCalls
import com.beyondeye.kbloc.compose.navigator.Navigator
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.core.BlocBase
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.coroutines.runBlocking
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

