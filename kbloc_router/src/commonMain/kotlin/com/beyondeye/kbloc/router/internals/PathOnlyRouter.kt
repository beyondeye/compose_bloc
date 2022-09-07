package com.beyondeye.kbloc.router.internals

import com.beyondeye.kbloc.router.Path
import com.beyondeye.kbloc.router.Router
import com.beyondeye.kbloc.router.RoutingResolver

/**
 * used to buffer path obtained from real router, inside [RoutingResolver]
 */
internal class PathOnlyRouter( private var curPathRaw:String?) : Router {
    constructor(other:Router) :this(other.getCurrentRawPath(""))
    override val curPath: Path
        get() = Path.from(curPathRaw ?: "")

    override fun getCurrentRawPath(initPath: String): String =
        if(curPathRaw.isNullOrEmpty()) initPath else curPathRaw!!

    override fun navigate(to: String, hide: Boolean) {
        curPathRaw = to
    }
}