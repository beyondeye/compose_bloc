package cafe.adriel.voyager.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import cafe.adriel.voyager.core.screen.Screen

/**
 * RootNavigator for desktop need to defined at the Window level
 */
@Composable
public fun FrameWindowScope.RootNavigator(
    screens: List<Screen>,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = { true },
    content: NavigatorContent = { CurrentScreen() }
) {
    //TODO *DARIO* implement lifecycle management similar to what is done for android RootNavigator
    // see https://github.com/JetBrains/compose-jb/tree/master/tutorials/Window_API_new
    _init_kbloc_for_subtree{
        Navigator(screens,disposeBehavior,onBackPressed,content)
    }
}