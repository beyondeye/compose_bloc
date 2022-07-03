package com.beyondeye.kbloc.stream

import com.beyondeye.kbloc.core.Bloc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

interface StreamEvent

class Subscribe : StreamEvent

class OnData(val data:Int) :StreamEvent

class StreamBloc(cscope:CoroutineScope, val inputEventsStream: Flow<Int>) :Bloc<StreamEvent,Int>(cscope,0) {
    private var _subscription: Job? = null
    init {
        on<StreamEvent> { _, emit ->
            _subscription?.cancel()
            _subscription = cscope.async {
                inputEventsStream.collect { value ->
                    delay(100)
                    add(OnData(value)) //new event
                }
            }
        }
        on<OnData> { event, emit ->
            emit(event.data)
        }
    }

    override suspend fun close() {
        _subscription?.cancel()
        _subscription=null
        super.close()
    }
}
