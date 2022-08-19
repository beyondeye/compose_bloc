package com.beyondeye.kbloc.counter

import com.beyondeye.kbloc.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.merge

val customTransformer:EventTransformer<CounterEvent> = { events, mapper ->
    val nonDebounceStream = events.filter { it !=CounterEvent.increment }
    // about the throttle operator in rxjava and kotlin, see
    //https://proandroiddev.com/from-rxjava-to-kotlin-flow-throttling-ed1778847619
    val debounceStream = events.filter { it == CounterEvent.increment }.throttleFirst(100)

    merge(nonDebounceStream,debounceStream).concurrentAsyncExpand(mapper)
}



class MergeBloc(cscope:CoroutineScope,val  onTransitionCallback: onTransitionCallback?=null) : Bloc<CounterEvent, Int>(cscope,0,
    false,false) {
    init {
        on<CounterEvent>(transformer = customTransformer, handler = ::_onCounterEvent)
    }

    override fun onTransition(transition: Transition<CounterEvent, Int>) {
        super.onTransition(transition)
        onTransitionCallback?.invoke(transition)
    }

    fun _onCounterEvent(event:CounterEvent,emit:Emitter<Int>) {
        when(event) {
            CounterEvent.increment -> emit(state + 1)
            CounterEvent.decrement -> emit( state -1)
        }
    }
}
