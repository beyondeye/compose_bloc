package com.beyondeye.kbloc.router

import cafe.adriel.voyager.core.screen.Screen

public class RoutingResolver(public val defaultRoute: String, private val routingDefinition:  RouteBuilder.() -> Screen?) {
    public fun resolveFor(router: Router): Screen? {
        var res: Screen?
        do {
            //get current route from router, or defaultRoute, if current route undefined
            val rawPath =router.getPath(defaultRoute)
            val path = Path.from(rawPath)
            val node = RouteBuilder(router, path.path, path)
            res=node.routingDefinition() //return value
        } while(res=== __Redirect)
        return res
    }
}