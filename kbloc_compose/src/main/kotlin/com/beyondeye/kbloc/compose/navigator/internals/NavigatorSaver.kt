package com.beyondeye.kbloc.compose.navigator.internals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import com.beyondeye.kbloc.compose.navigator.Navigator
import com.beyondeye.kbloc.compose.navigator.NavigatorDisposeBehavior
import com.beyondeye.kbloc.compose.screen.Screen

internal val LocalNavigatorStateHolder: ProvidableCompositionLocal<SaveableStateHolder> =
    staticCompositionLocalOf { error("LocalNavigatorStateHolder not initialized") }

@Composable
internal fun rememberNavigator(
    screens: List<Screen>,
    disposeBehavior: NavigatorDisposeBehavior,
    parent: Navigator?
): Navigator {
    val stateHolder = LocalNavigatorStateHolder.current

    return rememberSaveable(saver = navigatorSaver(stateHolder, disposeBehavior, parent)) {
        Navigator(screens, stateHolder, disposeBehavior, parent)
    }
}

private fun navigatorSaver(
    stateHolder: SaveableStateHolder,
    disposeBehavior: NavigatorDisposeBehavior,
    parent: Navigator?
): Saver<Navigator, Any> =
    listSaver(
        save = { navigator -> navigator.items },
        restore = { items -> Navigator(items, stateHolder, disposeBehavior, parent) }
    )
