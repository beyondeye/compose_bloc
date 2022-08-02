package com.beyondeye.kbloc.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

public fun <T> Iterable<Deferred<T>>.awaitAny(scope: CoroutineScope): CompletableDeferred<T> {
    val completer = CompletableDeferred<T>()
    for (future in this) {
        scope.launch {
            try {
                val value = future.await()
                if (!completer.isCompleted) completer.complete(value)
            } catch (e: Throwable) {
                if (!completer.isCompleted) completer.completeExceptionally(e)
            }
        }
    }
    return completer
}


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





//for clearer code when translating dart code
public  suspend fun Job.await() {
    this.join()
}
