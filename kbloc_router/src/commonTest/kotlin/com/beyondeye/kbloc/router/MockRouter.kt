package com.beyondeye.kbloc.router

import cafe.adriel.voyager.core.screen.Screen

public class MockRouter : Router {

    override val currentPath: Path
        get() = Path.from(currenPathRaw?:"")

    private var currenPathRaw:String? =null

    override fun getPath(initPath: String):String =
        currenPathRaw ?: initPath

    override fun navigate(to: String, hide: Boolean) {
        currenPathRaw = to
    }
}

operator public fun MockRouter.invoke(initPath: String, routeBuilder: RouteBuilder.() -> Screen?):(String)->Screen? =
    route(initPath, routeBuilder)
