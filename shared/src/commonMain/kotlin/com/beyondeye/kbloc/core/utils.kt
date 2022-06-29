package com.beyondeye.kbloc.core

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch

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
