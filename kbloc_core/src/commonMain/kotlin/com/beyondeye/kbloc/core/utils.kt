package com.beyondeye.kbloc.core

import kotlinx.coroutines.*
public const val LOGTAG:String="*kbloc*"

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








//for clearer code when translating dart code
public  suspend fun Job.await() {
    this.join()
}
