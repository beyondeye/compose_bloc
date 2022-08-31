package cafe.adriel.voyager.core.lifecycle

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import kotlinx.atomicfu.update
import kotlinx.collections.immutable.persistentHashMapOf

//import cafe.adriel.voyager.core.concurrent.ThreadSafeMap
public object ScreenLifecycleStore {

    //atomicfu atomic data should not be public according to atomicfu docs
    private val owners = atomic(persistentHashMapOf<ScreenKey, ScreenLifecycleOwner>())

    public fun get(
        screen: Screen,
        factory: (ScreenKey) -> ScreenLifecycleOwner
    ): ScreenLifecycleOwner {
        val key=screen.key
        val existing = owners.value[key]
        existing?.let { return it }
        val created=factory(key)
        owners.update { it.put(key,created) }
        return created
    }

    public fun remove(screen: Screen) {
        val key=screen.key
        val toremove= owners.value[key]
        owners.update { it.remove(key) }
        toremove?.onDispose(screen)
    }
}
