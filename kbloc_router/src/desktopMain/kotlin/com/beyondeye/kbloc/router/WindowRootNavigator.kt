package com.beyondeye.kbloc.router

import androidx.compose.runtime.*
import androidx.compose.ui.window.FrameWindowScope
import cafe.adriel.voyager.navigator.*
import com.beyondeye.kbloc.compose.lifecycle.mp_collectAsStateWithLifecycle
import com.beyondeye.kbloc.router.internals.StateFlowRouter
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * RootNavigator for desktop need to be defined at the Window level
 * this navigator is based on [routeResolver] and expose a [LocalNavigatorRouter] compositionlocal variable
 * with the router that can be used for navigating to screen associated to specific url
 */
@Composable
public fun FrameWindowScope.RootNavigator(
    routeResolver: RouteResolver,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = { true },
) {
    //find the initial screen (the screen for the default path
    val initialScreen = remember { routeResolver.resolveFor("") }
    val initialPath = remember { routeResolver.defaultRoute }
    val curPathRawFlow = remember { MutableStateFlow(initialPath) }
    val router = remember { StateFlowRouter(curPathRawFlow) }
    //initialize root navigator
    _init_kbloc_for_subtree(LocalNavigatorRouter.provides(router))
    {
        Navigator(initialScreen, disposeBehavior, onBackPressed) { nav: Navigator ->
            CurrentScreen() //show current screen
            //listen to changes of path
            val curPathRaw = curPathRawFlow.mp_collectAsStateWithLifecycle(rememberCoroutineScope())
            LaunchedEffect(curPathRaw) {
                val routedScreen = routeResolver.resolveFor(router)
                //TODO define if and which of previous screen should be popped when new screen is opened
                // think for example of what is possible in androidx.navigation:navigation-compose
                // see https://developer.android.com/jetpack/compose/navigation
                nav.push(routedScreen)
            }
        }
    }
}