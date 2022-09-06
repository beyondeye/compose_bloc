package com.beyondeye.kbloc.router.utils

import com.beyondeye.kbloc.router.Path
import com.beyondeye.kbloc.router.Router

public class MockRouter : Router {

    override val currentPath: Path
        get() = Path.from(currenPathRaw ?: "")

    private var currenPathRaw:String? =null

    override fun getPath(initPath: String):String =
        if(currenPathRaw.isNullOrEmpty()) initPath else currenPathRaw!!

    override fun navigate(to: String, hide: Boolean) {
        currenPathRaw = to
    }
}