package com.beyondeye.kbloc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

//see https://github.com/Kotlin/kotlinx-datetime
// see https://proandroiddev.com/from-rxjava-to-kotlin-flow-throttling-ed1778847619
// note that this throttleFirst implementation does not respect TestCoroutineScope.
public fun <T> Flow<T>.throttleFirst(periodMillis: Long): Flow<T> {
    require(periodMillis > 0) { "period should be positive" }
    return flow {
        var lastTime = 0L
        collect { value ->
            val currentTime =  Clock.System.now().toEpochMilliseconds() //System.currentTimeMillis()
            if (currentTime - lastTime >= periodMillis) {
                lastTime = currentTime
                emit(value)
            }
        }
    }
}