package com.beyondeye.kbloc.compose.model

import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.key
import com.beyondeye.kbloc.compose.concurrent.ThreadSafeMap
import com.beyondeye.kbloc.compose.navigator.Navigator
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.core.BlocBase
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private typealias BlocKey = String

public class BlocStore {

    /**
     * a list of currently active [ScreenModel] instances. an active instance is an instance that
     * is created/injected with method [Screen.rememberScreenModel] that is usually called at the
     * beginning of [Screen.Content] method implementation.
     * When a [Screen] is popped from the [Navigator] stack the method [ScreenModel.onDispose] is
     * called and it is removed from this map
     */
    @PublishedApi
    internal var blocs: PersistentMap<BlocKey, BlocBase<*>> = persistentHashMapOf()

    @PublishedApi
    internal val blocs_mutex = Mutex()

    /**
     * key for a [ScreenModel] of a specific type T for a specific [screen], with an additional
     * optional user defined [tag] appended in the end, in case there are more than one [ScreenModel]
     * of the same type for this [Screen]
     */
    @PublishedApi
    internal inline fun <reified T : BlocBase<*>> getKey(screen: Screen, tag: String?): BlocKey =
        "${screen.key}:${T::class.qualifiedName}:${tag ?: "default"}"

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


    /**
     * method that is called from [Navigator.dispose] when a [Screen] is popped from the navigation
     * stack
     */
    public fun remove(screen: Screen) {
        val bloc_keys_to_remove = blocs.entries.mapNotNull {
            val k = it.key
            if (k.startsWith(screen.key)) k else null
        }
        //todo instead of runBlocking run it in a separate thread?
        //todo also separe
        runBlocking {
            for (key in bloc_keys_to_remove) {
                blocs[key]?.close() //onDispose()
                blocs_mutex.withLock {
                    blocs = blocs.remove(key)
                }
            }
        }
    }
}

