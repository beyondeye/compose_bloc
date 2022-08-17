package cafe.adriel.voyager.navigator.internal

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
    androidx.activity.compose.BackHandler(enabled,onBack)
}
