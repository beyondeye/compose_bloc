package com.beyondeye.kbloc.router

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import com.beyondeye.kbloc.router.internals.PathOnlyRouter

private object EmptyScreen:Screen {
    @Composable
    override fun Content() {
    }
}


public class RoutingResolver(public val defaultRoute: String, private val routingDefinition:  RoutingDefBuilder.() -> Screen?) {

    public var enableCheckForPathSegmentsExtraSlashes:Boolean=true
    /**
     * if[enableCheckForPathSegmentsExtraSlashes] is true then check if path segments specified
     * in route("/path_seg") {} and in redirect, wrongly contains more than one path segments
     * or contains slashes at beginning and/or end of the path segments
     * For initial testing this flag is recommended to be kept on. In production, for best
     * performance of route resolution you should set this flag false and define the routes segments
     * without extra slashes
     */
    public fun resolveFor(router: Router): Screen {
        var res: Screen?
        //get path from input router in an internal router class used for route resolving
        val _po_router=PathOnlyRouter(router)
        var foundRedirect=false
        while(true) {
            //get current route from router, or defaultRoute, if current route undefined
            val rawPath =_po_router.getCurrentRawPath(defaultRoute)
            val path = Path.from(rawPath)
            val node = RoutingDefBuilder(_po_router, path.path, path,enableCheckForPathSegmentsExtraSlashes)
            res=node.routingDefinition() //return value
            if(res!==__Redirect) break
            foundRedirect=true
        }
        //if redirect, update the parent router with the new path
        //TODO this will probably trigger a new useless double recomposition (only for redirect): how can we avoid this? we need to check
        if(foundRedirect) router.navigate(_po_router.getCurrentRawPath(""))
        return res ?:EmptyScreen
    }
    public fun resolveFor(pathRaw:String):Screen {
        return resolveFor(PathOnlyRouter(pathRaw))
    }
}