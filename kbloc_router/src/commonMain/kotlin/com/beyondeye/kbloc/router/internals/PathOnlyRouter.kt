package com.beyondeye.kbloc.router.internals

import com.beyondeye.kbloc.router.Path
import com.beyondeye.kbloc.router.Router
import com.beyondeye.kbloc.router.RoutingResolver

/**
 * used to buffer path obtained from real router, inside [RoutingResolver]
 */
internal class PathOnlyRouter( private var currenPathRaw:String?) : Router {
    constructor(other:Router) :this(other.getCurrentRawPath(""))
    override val currentPath: Path
        get() = Path.from(currenPathRaw ?: "")

    override fun getCurrentRawPath(initPath: String): String =
        if(currenPathRaw.isNullOrEmpty()) initPath else currenPathRaw!!

    override fun navigate(to: String, hide: Boolean) {
        currenPathRaw = to
    }
}