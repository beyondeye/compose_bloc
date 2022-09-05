package com.beyondeye.kbloc.router
//original code from https://github.com/hfhbd/routing-compose
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import kotlin.jvm.*

/**
 * *DARIO* abstraction of the Router that has multiple impl
 * as [BrowserRouter], [DesktopRouter], [HashRouter] and so on
 */
public interface Router {
    /**
     * The current path
     */
    public val currentPath: Path

    public fun navigate(to: String, hide: Boolean = false)

    public fun getPath(initPath: String): String
}

internal val RouterCompositionLocal: ProvidableCompositionLocal<Router> =
    compositionLocalOf { error("Router not defined, cannot provide through RouterCompositionLocal.") }

private object __Redirect:Screen {
    @Composable
    override fun Content() {
        TODO("Not yet implemented")
    }

}

public class RoutingResolver(private val routingDefinition:  RouteBuilder.() -> Screen?) {
    public operator fun invoke(router:Router,routeToResolve:String):Screen? {
        val rawPath =router.getPath(routeToResolve)
        val path = Path.from(rawPath)
        val node = RouteBuilder(router,path.path, path)
        var res:Screen?
        do {
            res=node.routingDefinition() //return value
        } while(res===__Redirect)
        return res
    }
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
