package cafe.adriel.voyager.navigator.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import com.beyondeye.kbloc.compose.bloc.internals.BlocStore
import com.beyondeye.kbloc.compose.bloc.internals.LocalBlocStoreOwner
import com.beyondeye.kbloc.compose.model.ScreenModelStore
import com.beyondeye.kbloc.compose.model.internals.LocalScreenModelStoreOwner
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
    val screenModelStore = LocalScreenModelStoreOwner.current.screenModelStore
    val blocStore = LocalBlocStoreOwner.current.blocStore

    return rememberSaveable(saver = navigatorSaver(stateHolder, disposeBehavior, parent,screenModelStore,blocStore)) {
        Navigator(screens, stateHolder, disposeBehavior, parent,screenModelStore,blocStore)
    }
}

private fun navigatorSaver(
    stateHolder: SaveableStateHolder,
    disposeBehavior: NavigatorDisposeBehavior,
    parent: Navigator?,
    screenModelStore: ScreenModelStore,
    blocStore: BlocStore
): Saver<Navigator, Any> =
    listSaver(
        save = { navigator -> navigator.items },
        restore = { items -> Navigator(items, stateHolder, disposeBehavior, parent,screenModelStore,blocStore) }
    )
