package com.beyondeye.kbloc.router

import cafe.adriel.voyager.core.screen.Screen

public class RoutingResolver(public val defaultRoute: String, private val routingDefinition:  RouteBuilder.() -> Screen?) {

    /**
     * if[enableCheckForPathSegmentsExtraSlashes] is true then check if path segments specified
     * in route("/path_seg") {} and in redirect, wrongly contains more than one path segments
     * or contains slashes at beginning and/or end of the path segments
     * For initial testing this flag is recommended to be kept on. In production, for best
     * performance of route resolution you should set this flag false and define the routes segments
     * without extra slashes
     */
    public fun resolveFor(router: Router,enableCheckForPathSegmentsExtraSlashes:Boolean=true): Screen? {
        var res: Screen?
        do {
            //get current route from router, or defaultRoute, if current route undefined
            val rawPath =router.getPath(defaultRoute)
            val path = Path.from(rawPath)
            val node = RouteBuilder(router, path.path, path,enableCheckForPathSegmentsExtraSlashes)
            res=node.routingDefinition() //return value
        } while(res=== __Redirect)
        return res
    }
}