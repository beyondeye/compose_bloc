package com.beyondeye.kbloc.router.internals

import com.beyondeye.kbloc.router.Path
import com.beyondeye.kbloc.router.Router
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * router then when [navigate] is called, updated the current value of the wrapped [currentUrl] stateflow
 * Except for this, it does nothing
 */
internal class StateFlowRouter(private val currentUrl:MutableStateFlow<String>) :Router {
    init {
        currentUrl.value=  ""
    }
    override val curPath: Path
        get() = Path.from(currentUrl.value)

    //todo: should I return currentHash.value instead here?
    override fun getCurrentRawPath(initPath: String): String {
        val cur=currentUrl.value
        return if(cur.isEmpty()) initPath else cur
    }

    override fun navigate(to: String, hide: Boolean) {
        //TODO need to understand what hide do
        if (hide) {
            currentUrl.value = to
        } else {
            currentUrl.value = to
        }
    }

}
