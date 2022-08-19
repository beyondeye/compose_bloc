package com.beyondeye.kbloc.counter

import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.Emitter
import kotlinx.coroutines.CoroutineScope

/**
 * NOTE: in kotlin there is no much difference between Error and Exception like in Dart
 * so [CounterErrorBloc] and [CounterExceptionBloc] are basically the same thing
 */
class CounterExceptionBloc(cscope: CoroutineScope) : Bloc<CounterEvent, Int>(cscope,0,false,false) {
    init {
        on<CounterEvent> { event, emit ->
            _onCounterEvent(event,emit)
        }
    }
    fun _onCounterEvent(event:CounterEvent,emit: Emitter<Int>) {
        when(event) {
            CounterEvent.decrement -> emit(state - 1)
            CounterEvent.increment -> throw Exception("fatal exception")
        }
    }
}

