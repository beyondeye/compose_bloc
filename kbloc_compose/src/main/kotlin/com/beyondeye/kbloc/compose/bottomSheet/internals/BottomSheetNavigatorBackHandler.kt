package com.beyondeye.kbloc.compose.bottomSheet.internals

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import com.beyondeye.kbloc.compose.bottomSheet.BottomSheetNavigator

@Composable
internal /*actual*/ fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) =
    androidx.activity.compose.BackHandler(enabled, onBack)

/*
@Composable
internal expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
@Composable
internal actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) =
    BackHandler(enabled, onBack)

  // for desktop
  @Composable internal actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
 */
/*
    //for android

 */

@ExperimentalMaterialApi
@Composable
internal fun BottomSheetNavigatorBackHandler(
    navigator: BottomSheetNavigator,
    sheetState: ModalBottomSheetState,
    hideOnBackPress: Boolean
) {
    if (sheetState.isVisible) {
        BackHandler {
            if (navigator.pop().not() && hideOnBackPress) {
                navigator.hide()
            }
        }
    }
}
