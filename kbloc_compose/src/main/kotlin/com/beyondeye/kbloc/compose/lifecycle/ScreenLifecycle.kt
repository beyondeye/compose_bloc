package com.beyondeye.kbloc.compose.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.beyondeye.kbloc.compose.screen.Screen

//
//
/**
 * *DARIO* this is simply a disposable effect for the screen and it is not doing what is expected
 * see for example https://github.com/adrielcafe/voyager/issues/42
 * see also https://voyager.adriel.cafe/lifecycle
 * So now it is deprecated
 * the correct way to listen to Lifecyle events is as described here:
 * https://developer.android.com/jetpack/compose/side-effects#disposableeffect
 */
@Composable
@Deprecated(
    "Not Working, substitute with what described at https://developer.android.com/jetpack/compose/side-effects#disposableeffect",
    level=DeprecationLevel.ERROR)
public fun Screen.LifecycleEffect(
    onStarted: () -> Unit = {},
    onDisposed: () -> Unit = {}
) {
    DisposableEffect(key) {
        onStarted()
        onDispose(onDisposed)
    }
}

@Composable
public fun rememberScreenLifecycleOwner(
    screen: Screen
): ScreenLifecycleOwner =
    remember(screen.key) {
        when (screen) {
            is ScreenLifecycleProvider -> screen.getLifecycleOwner()
            else -> DefaultScreenLifecycleOwner
        }
    }

public interface ScreenLifecycleProvider {

    public fun getLifecycleOwner(): ScreenLifecycleOwner
}
