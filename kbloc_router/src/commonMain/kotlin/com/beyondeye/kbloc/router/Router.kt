package com.beyondeye.kbloc.router
//original code from https://github.com/hfhbd/routing-compose
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import kotlin.jvm.*

/**
 * obtain the router (associated with the root navigator) defined for the current
 * composable subtree. It can be used to issue navigate() command to specific named
 * destination. The resolved screen will be pushed in the navigation stack of the root
 * navigator
 */
public val LocalNavigatorRouter: ProvidableCompositionLocal<Router?> =
    staticCompositionLocalOf { null }

/**
 * *DARIO* abstraction of the Router that has multiple impl
 * as [BrowserRouter], [DesktopRouter], [HashRouter] and so on
 */
public interface Router {
    /**
     * The current path
     */
    public val curPath: Path

    public fun navigate(to: String, hide: Boolean = false)

    /**
     * if current path is null or empty, return [initPath]
     */
    public fun getCurrentRawPath(initPath: String): String
}



public fun Router.navigate(to: String, parameters: Parameters, hide: Boolean = false) {
    navigate("$to?$parameters", hide = hide)
}

@JvmName("navigateParameterList")
public fun Router.navigate(to: String, parameters: Map<String, List<String>>, hide: Boolean = false) {
    navigate(to, Parameters.from(parameters), hide = hide)
}

public fun Router.navigate(to: String, parameters: Map<String, String>, hide: Boolean = false) {
    navigate(to, Parameters.from(parameters), hide = hide)
}
