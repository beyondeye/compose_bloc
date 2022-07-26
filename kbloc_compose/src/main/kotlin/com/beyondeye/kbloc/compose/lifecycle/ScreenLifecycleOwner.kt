package com.beyondeye.kbloc.compose.lifecycle

import androidx.compose.runtime.Composable
import com.beyondeye.kbloc.compose.screen.Screen

public interface ScreenLifecycleOwner {

    @Composable
    public fun getHooks(): ScreenLifecycleHooks = ScreenLifecycleHooks.Empty

    public fun onDispose(screen: Screen) {}
}

internal object DefaultScreenLifecycleOwner : ScreenLifecycleOwner
