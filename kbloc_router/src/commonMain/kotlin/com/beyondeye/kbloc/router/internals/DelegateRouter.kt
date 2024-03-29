package com.beyondeye.kbloc.router.internals

import com.beyondeye.kbloc.router.Router

//original code from https://github.com/hfhbd/routing-compose

internal class DelegateRouter(val basePath: String, val router: Router) : Router by router {
    override fun navigate(to: String, hide: Boolean) {
        when {
            to.startsWith("/") -> {
                router.navigate(to, hide)
            }

            basePath == "/" -> {
                router.navigate("/$to", hide)
            }

            to.startsWith(".") -> {
                val newPath = router.curPath.relative(to)
                router.navigate(newPath.path)
            }

            else -> {
                router.navigate("$basePath/$to", hide)
            }
        }
    }
}
