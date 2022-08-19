package com.beyondeye.kbloc.counter

import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.Emitter
import com.beyondeye.kbloc.core.Transition
import kotlinx.coroutines.CoroutineScope


enum class CounterEvent {
    increment,
    decrement
}
typealias OnEventCallback = (CounterEvent)->Unit
typealias onTransitionCallback = (Transition<CounterEvent,Int>)-> Unit
typealias onErrorCallback = (Throwable)-> Unit

class CounterBloc(cscope:CoroutineScope,
                  val onEventCallback: OnEventCallback?=null,
                  val onTransitionCallback: onTransitionCallback?=null,
                  val onErrorCallback: onErrorCallback?=null
)            :Bloc<CounterEvent,Int>(cscope,0,false,false)
{
    init {
        on<CounterEvent> { event, emit ->
            _onCounterEvent(event,emit)
        }
    }
    override fun onEvent(event: CounterEvent) {
        super.onEvent(event)
        onEventCallback?.invoke(event)
    }

    override fun onTransition(transition: Transition<CounterEvent, Int>) {
        super.onTransition(transition)
        onTransitionCallback?.invoke(transition)
    }

    override fun onError(error: Throwable) {
        onErrorCallback?.invoke(error)
        super.onError(error)
    }
    private fun _onCounterEvent(event: CounterEvent, emit: Emitter<Int>) {
        when(event) {
            CounterEvent.increment -> emit(state + 1)
            CounterEvent.decrement -> emit(state -1)
        }
    }
}
