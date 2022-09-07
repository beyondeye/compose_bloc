package com.beyondeye.kbloc.router

import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.*
import com.beyondeye.kbloc.compose.lifecycle.mp_collectAsStateWithLifecycle
import kotlinx.browser.document
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.renderComposable
import org.jetbrains.compose.web.renderComposableInBody
import org.w3c.dom.Element
import org.w3c.dom.HTMLBodyElement
import org.w3c.dom.get

/**
 * obtain the router (associated with the root navigator) defined for the current
 * composable subtree. It can be used to issue navigate() command to specific named
 * destination. The resolved screen will be pushed in the navigation stack of the root
 * navigator
 */
public val LocalRouter: ProvidableCompositionLocal<Router?> =
    staticCompositionLocalOf { null }


/**
 * for JS a composable can be attached to any dom element in the page.
 * with method renderComposable, so instead of defining a root navigator
 * we define custom renderComposable methods that also initialize a rootnavigator
 */
@Composable
private fun <TElement : Element> renderComposableWithRouterImpl(
    root: TElement,
    monotonicFrameClock: MonotonicFrameClock = DefaultMonotonicFrameClock,
    routingResolver: RoutingResolver,
    useHashRouter: Boolean,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = { true } //navigator onBackPressed override
): Composition {
    val content_w_router: @Composable DOMScope<TElement>.() -> Unit = {
        //find the initial screen (the screen for the default path
        val initialScreen = remember { routingResolver.resolveFor("") }
        val initialPath = remember { routingResolver.defaultRoute }
        val curPathRawFlow = remember { MutableStateFlow(initialPath) }
        val router = remember {
            if (useHashRouter) BrowserHashRouter(curPathRawFlow) else BrowserRouter(curPathRawFlow)
        }
        //setup javascript listener for when the browser path is changed
        DisposableEffect(true) {
            router.setupRawPathListener(initialPath)
            onDispose { router.removeRawPathListener() }
        }
        //initialize root navigator
        init_kbloc_for_subtree(LocalDomScope.provides(this), LocalRouter.provides(router))
        {
            Navigator(initialScreen, disposeBehavior, onBackPressed) { nav: Navigator ->
                CurrentScreen() //show current screen
                //listen tho changes of
                val curPathRaw = curPathRawFlow.mp_collectAsStateWithLifecycle(rememberCoroutineScope())
                LaunchedEffect(curPathRaw) {
                    val routedScreen = routingResolver.resolveFor(router)
                    //TODO define if and which of previous screen should be popped when new screen is opened
                    // think for example of what is possible in androidx.navigation:navigation-compose
                    // see https://developer.android.com/jetpack/compose/navigation
                    nav.push(routedScreen)
                }
            }
        }
    }
    return renderComposable(root, monotonicFrameClock, content_w_router)
}

/**
 * Use this method to mount the composition at the [HTMLBodyElement] of the current document
 * this method is the same as [renderComposableInBody] but also initialize the root kbloc-navigator
 * as a router navigator,
 *
 * @param screens - navigator initial nav stack (at list one screen)
 *
 * if [useHashRouter] is true then use hashed urls (e.g. yoursite.com/#/path)
 * see also [BrowserRouter] and [BrowserHashRouter] documentation for more details
 * @return the instance of the [Composition]
 */
//TODO add a flag to specify with type of router: HashRouter or BrowserRouter. Also need to document  the server configuration for the BrowserRouter to work (See documemtation in router compose)
@Composable
public fun renderComposableInBodyWithRouter(
    routingResolver: RoutingResolver,
    useHashRouter:Boolean=true,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = { true }, //navigator onBackPressed override
): Composition = renderComposableWithRouterImpl(
    document.getElementsByTagName("body")[0] as HTMLBodyElement,
    DefaultMonotonicFrameClock,routingResolver,
    useHashRouter,
    disposeBehavior,onBackPressed,
)