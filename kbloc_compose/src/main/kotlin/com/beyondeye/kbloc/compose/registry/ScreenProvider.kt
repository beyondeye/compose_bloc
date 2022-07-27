package com.beyondeye.kbloc.compose.registry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.beyondeye.kbloc.compose.screen.Screen

/**
 * see https://voyager.adriel.cafe/navigation/multi-module-navigation
 */
@Composable
public inline fun <reified T : ScreenProvider> rememberScreen(provider: T): Screen =
    remember(provider) {
        ScreenRegistry.get(provider)
    }

public interface ScreenProvider
