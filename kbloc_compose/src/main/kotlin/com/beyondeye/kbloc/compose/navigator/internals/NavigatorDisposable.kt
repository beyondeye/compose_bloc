package com.beyondeye.kbloc.compose.navigator.internals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.beyondeye.kbloc.compose.navigator.Navigator
import com.beyondeye.kbloc.compose.stack.StackEvent

private val disposableEvents: Set<StackEvent> =
    setOf(StackEvent.Pop, StackEvent.Replace)

@Composable
internal fun NavigatorDisposableEffect(
    navigator: Navigator
) {
    DisposableEffect(navigator) {
        onDispose {
            for (screen in navigator.items) {
                navigator.dispose(screen)
            }
            navigator.clearEvent()
        }
    }
}

@Composable
internal fun StepDisposableEffect(
    navigator: Navigator
) {
    val currentScreen = navigator.lastItem

    DisposableEffect(currentScreen.key) {
        onDispose {
            if (navigator.lastEvent in disposableEvents) {
                navigator.dispose(currentScreen)
                navigator.clearEvent()
            }
        }
    }
}
