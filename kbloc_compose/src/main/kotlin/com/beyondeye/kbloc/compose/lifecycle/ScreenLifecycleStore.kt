package com.beyondeye.kbloc.compose.lifecycle

import com.beyondeye.kbloc.compose.concurrent.ThreadSafeMap
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.compose.screen.ScreenKey

public object ScreenLifecycleStore {

    private val owners = ThreadSafeMap<ScreenKey, ScreenLifecycleOwner>()

    public fun get(
        screen: Screen,
        factory: (ScreenKey) -> ScreenLifecycleOwner
    ): ScreenLifecycleOwner =
        owners.getOrPut(screen.key) { factory(screen.key) }

    public fun remove(screen: Screen) {
        owners.remove(screen.key)?.onDispose(screen)
    }
}
