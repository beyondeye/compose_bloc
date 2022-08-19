package com.beyondeye.kbloc.stream

import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.await
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.switchMap


interface RestartableStreamEvent

class ForEach : RestartableStreamEvent

class ForEachOnError:RestartableStreamEvent

class ForEachTryCatch :RestartableStreamEvent

class ForEachCatchError :RestartableStreamEvent

class UnawaitedForEach :RestartableStreamEvent

class OnEach :RestartableStreamEvent

class OnEachOnError :RestartableStreamEvent

class OnEachTryCatch :RestartableStreamEvent

class OnEachTryCatchAbort :RestartableStreamEvent

class OnEachCatchError :RestartableStreamEvent

class UnawaitedOnEach :RestartableStreamEvent

const val _delay_msecs= 100L

/** Handles errors emitted by this [Job].
 *
 * This is the asynchronous equivalent of a "catch" block.
 *
 * Returns a new [Job] that will be completed with either the result of
 * this one or the result of calling the `onError` callback.
 *
 * If this [Job] completes with a value,
 * the returned [Job] completes with the same value.
 *
 * If this [Job] completes with an error,
 * then [test] is first called with the error value.
 *
 * If [test] returns false, the exception is not handled by this [catchError],
 * and the returned future completes with the same error and stack trace
 * as this future.
 *
 * If [test] returns `true`,
 * [onError] is called with the error and possibly stack trace,
 * and the returned Job is completed with the result of this call
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
public fun Job.catchError(test:((Throwable)->Boolean)?=null,onError:(Throwable)->Unit ):Job {
    invokeOnCompletion { cause ->
        if(cause==null || cause is CancellationException) return@invokeOnCompletion
        test?.let { if(!test(cause)) return@invokeOnCompletion }
        //TODO when onError is run, the result of onError should be returned by this job in stead of the throwed exception
        TODO()
        onError(cause)
    }
    return this
}

class  RestartableStreamBloc(cscope:CoroutineScope,val inputEventsStream: Flow<Int>) :Bloc<RestartableStreamEvent,Int>(cscope,0,
    false,false) {
    init {
        //    message = "Flow analogues of 'switchMap' are 'transformLatest', 'flatMapLatest' and 'mapLatest'",
        on<ForEach>(transformer = { events, mapper -> events.flatMapLatest(mapper) })
        { _, emit ->
            emit.forEach( inputEventsStream, onData =  {it }).await()
        }
        on<ForEachOnError>(transformer = { events, mapper -> events.flatMapLatest(mapper) })
        { _, emit ->
            try {
                emit.forEach( inputEventsStream, onData =  {it }, onError = {_-> -1}).await()
            }
            catch (e:Throwable) {
                emit(-1)
            }
        }
        on<ForEachTryCatch>(transformer = { events, mapper -> events.flatMapLatest(mapper) })
        { _, emit ->
            try {
                emit.forEach( inputEventsStream, onData =  {it }).await()
            }
            catch (e:Throwable) {
                emit(-1)
            }
        }

        on<ForEachCatchError>(transformer = { events, mapper -> events.flatMapLatest(mapper) })
        { _, emit ->
            emit.forEach( inputEventsStream, onData =  {it }).catchError { error -> emit(-1) }
        }
        on<UnawaitedForEach>(transformer = { events, mapper -> events.flatMapLatest(mapper) })
        { _, emit ->
            emit.forEach( inputEventsStream, onData =  {it })
        }

        on<OnEach>(transformer = { events, mapper -> events.flatMapLatest(mapper) })
        { _, emit ->
            emit.onEach( inputEventsStream, onData =  {i ->
                cscope.async {
                    delay(_delay_msecs)
                    emit(i)
                }
            }).await()
        }

        on<OnEachOnError>(transformer = { events, mapper -> events.flatMapLatest(mapper) })
        { _, emit ->
            emit.onEach(inputEventsStream,
                onData = { i ->
                    cscope.async {
                        delay(_delay_msecs)
                        emit(i)
                    }
                },
                onError = { _ -> emit(-1) }).await()
        }

        on<OnEachTryCatch>(transformer = { events, mapper -> events.flatMapLatest(mapper) })
        { _, emit ->
            try {
                emit.onEach( inputEventsStream, onData =  {i ->
                    cscope.async {
                        delay(_delay_msecs)
                        emit(i)
                    }
                }).await()
            } catch (e:Throwable) {
                emit(-1)
            }
        }

        on<OnEachTryCatchAbort>(transformer = { events, mapper -> events.flatMapLatest(mapper) })
        { _, emit ->
            try {
                emit.onEach( inputEventsStream, onData =  {i ->
                    cscope.async {
                        delay(_delay_msecs)
                        if(emit.isDone()) return@async
                        emit(i)
                    }
                }).await()
            } catch (e:Throwable) {
                emit(-1)
            }
        }

        on<OnEachCatchError>(transformer = { events, mapper -> events.flatMapLatest(mapper) })
        { _, emit ->
            emit.onEach( inputEventsStream, onData =  {i ->
                cscope.async {
                    delay(_delay_msecs)
                    emit(i)
                }
            }).catchError { _ -> emit(-1) }
        }

        on<UnawaitedOnEach>(transformer = { events, mapper -> events.flatMapLatest(mapper) })
        { _, emit ->
            emit.onEach( inputEventsStream, onData =  {i ->
                cscope.async {
                    delay(_delay_msecs)
                    emit(i)
                }
            })
        }
    }
}


