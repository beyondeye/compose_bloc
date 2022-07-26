package com.beyondeye.kbloc.compose.navigator.internals

import androidx.compose.runtime.Composable
import com.beyondeye.kbloc.compose.navigator.Navigator
import com.beyondeye.kbloc.compose.navigator.OnBackPressed


@Composable
internal fun NavigatorBackHandler(
    navigator: Navigator,
    onBackPressed: OnBackPressed
) {
    if (onBackPressed != null) {
        BackHandler(
            enabled = navigator.canPop || navigator.parent?.canPop ?: false,
            onBack = {
                if (onBackPressed(navigator.lastItem)) {
                    if (navigator.pop().not()) {
                        navigator.parent?.pop()
                    }
                }
            }
        )
    }
}

@Composable
internal fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    TODO("should call  androidx.activity.compose.BackHandler\n")
}
