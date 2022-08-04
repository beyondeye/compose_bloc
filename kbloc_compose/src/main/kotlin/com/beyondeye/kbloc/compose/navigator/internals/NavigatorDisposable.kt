package com.beyondeye.kbloc.compose.navigator.internals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.beyondeye.kbloc.compose.navigator.Navigator
import com.beyondeye.kbloc.compose.stack.StackEvent

private val disposableEvents: Set<StackEvent> =
    setOf(StackEvent.Pop, StackEvent.Replace)

/**
 * *DARIO* this was not working, a Screen must be disposed only when it is
 * explicitely removed (call to [Navigator.pop]) not if it leaves the composition
 * otherwise in android where a screen can be removed from composition if for
 * example the device is rotated, this will cause a lot of problems with screenmodels
 * that are destroyed and recreated without any need:  see for example
 * https://github.com/adrielcafe/voyager/issues/62
 */
/*
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
 */

/**
 * *DARIO* I am not sure what this was for but is for sure not working, like NavigatorDisposableEffect
 * see the comment above
 */
/*
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
 */