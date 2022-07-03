package com.beyondeye.kbloc.stream

import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.await
import com.beyondeye.kbloc.core.catchError
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

class  RestartableStreamBloc(cscope:CoroutineScope,val inputEventsStream: Flow<Int>) :Bloc<RestartableStreamEvent,Int>(cscope,0) {
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


