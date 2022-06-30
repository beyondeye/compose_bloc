package com.beyondeye.kbloc.counter

import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.Emitter
import kotlinx.coroutines.CoroutineScope

class CountErrorBloc(cscope:CoroutineScope) : Bloc<CounterEvent,Int>(cscope,0) {
    init {
        on<CounterEvent> { event, emit ->
            _onCounterEvent(event,emit)
        }
    }
    fun _onCounterEvent(event:CounterEvent,emit:Emitter<Int>) {
        when(event) {
            CounterEvent.decrement -> emit(state - 1)
            CounterEvent.increment -> throw Exception()
        }
    }
}
