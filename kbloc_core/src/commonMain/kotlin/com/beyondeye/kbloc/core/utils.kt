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


/** Handles errors emitted by this [Future].
 *
 * This is the asynchronous equivalent of a "catch" block.
 *
 * Returns a new [Future] that will be completed with either the result of
 * this future or the result of calling the `onError` callback.
 *
 * If this future completes with a value,
 * the returned future completes with the same value.
 *
 * If this future completes with an error,
 * then [test] is first called with the error value.
 *
 * If `test` returns false, the exception is not handled by this `catchError`,
 * and the returned future completes with the same error and stack trace
 * as this future.
 *
 * If `test` returns `true`,
 * [onError] is called with the error and possibly stack trace,
 * and the returned future is completed with the result of this call
 * in exactly the same way as for [then]'s `onError`.
 *
 * If `test` is omitted, it defaults to a function that always returns true.
 * The `test` function should not throw, but if it does, it is handled as
 * if the `onError` function had thrown.
 *
 * Note that futures don't delay reporting of errors until listeners are
 * added. If the first `catchError` (or `then`) call happens after this future
 * has completed with an error then the error is reported as unhandled error.
 * See the description on [Future].
 *
 * Example:
 * ```dart
 * Future.delayed(
 *   const Duration(seconds: 1),
 *   () => throw 401,
 * ).then((value) {
 *   throw 'Unreachable';
 * }).catchError((err) {
 *   print('Error: $err'); // Prints 401.
 * }, test: (error) {
 *   return error is int && error >= 400;
 * });
 * ```
// The `Function` below stands for one of two types:
// - (dynamic) -> FutureOr<T>
// - (dynamic, StackTrace) -> FutureOr<T>
// Given that there is a `test` function that is usually used to do an
// `is` check, we should also expect functions that take a specific argument.
 */
public fun Job.catchError(handler:(Throwable)->Unit ):Job {
    invokeOnCompletion { cause ->
        if(cause==null || cause is CancellationException) return@invokeOnCompletion
        handler(cause)
    }
    return this
}


//for clearer code when translating dart code
public  suspend fun Job.await() {
    this.join()
}
