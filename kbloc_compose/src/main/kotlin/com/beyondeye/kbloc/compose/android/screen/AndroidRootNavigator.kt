package com.beyondeye.kbloc.compose.android.screen

import android.app.Activity
import androidx.compose.runtime.Composable
import com.beyondeye.kbloc.compose.navigator.*
import com.beyondeye.kbloc.compose.screen.Screen
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.beyondeye.kbloc.compose.model.LocalScreenModelStoreOwner
import com.beyondeye.kbloc.compose.model.ScreenModelStore
import com.beyondeye.kbloc.compose.model.ScreenModelStoreOwner

private class ActivityScreenModelStoreViewModel:ScreenModelStoreOwner,ViewModel() {
    override val screenModelStore: ScreenModelStore
        get() = store
    private val store:ScreenModelStore= ScreenModelStore()
    override fun onCleared() {
        aa //call ondispose for all what need to be disposed
        super.onCleared()
    }
}
/**
 * *DARIO*
 */
@Composable
public fun ComponentActivity.RootNavigator(screen: Screen,
                                  disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
                                  onBackPressed: OnBackPressed = { true },
                                  content: NavigatorContent = { CurrentScreen() }
) {
        RootNavigator(
            screens = listOf(screen),
            disposeBehavior = disposeBehavior,
            onBackPressed = onBackPressed,
            content = content
        )
    }
}

/**
 * *DARIO*
 * In order to
 */
@Composable
public fun ComponentActivity.RootNavigator(
    screens: List<Screen>,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = { true },
    content: NavigatorContent = { CurrentScreen() }
) {
    val activityScreenModelStore = ViewModelProvider(this).get(ActivityScreenModelStoreViewModel::class.java)
    CompositionLocalProvider(LocalScreenModelStoreOwner.provides(activityScreenModelStore)) {
        Navigator(
            screens = screens,
            disposeBehavior = disposeBehavior,
            onBackPressed = onBackPressed,
            content = content
        )
    }
}
