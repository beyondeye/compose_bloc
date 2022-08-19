package com.beyondeye.kbloc.counter

import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.Emitter
import com.beyondeye.kbloc.core.Transition
import kotlinx.coroutines.CoroutineScope

class OnTransitionErrorBloc(
    cscope: CoroutineScope,
    val error: Throwable,
    val onErrorCallback: onErrorCallback
) : Bloc<CounterEvent, Int>(cscope, 0,false,false) {
    init {
        on<CounterEvent>(handler = ::_onCounterEvent)
    }

    override fun onTransition(transition: Transition<CounterEvent, Int>) {
        super.onTransition(transition)
        throw error
    }

    override fun onError(error: Throwable) {
        onErrorCallback.invoke(error)
        super.onError(error)
    }
    private fun _onCounterEvent(event: CounterEvent, emit: Emitter<Int>) {
        when(event) {
            CounterEvent.increment -> emit(state + 1)
            CounterEvent.decrement -> emit(state -1)
        }
    }
}