package com.beyondeye.kbloc_app

import com.beyondeye.kbloc.core.Bloc
import kotlinx.coroutines.CoroutineScope

interface MultiCounterEvent
class MultiAddEvent(val counterIdx:Int, val value:Int):MultiCounterEvent
class MultiSubEvent(val counterIdx:Int,val value:Int):MultiCounterEvent
data class MultiCounterState(val counter:Array<Int>)

class MultiCounterBloc(
    cscope: CoroutineScope,
    startCounters: Array<Int> = Array(10) { 0 }
) :
    Bloc<MultiCounterEvent,
            MultiCounterState>(
        cscope, MultiCounterState(counter = startCounters),
        true
    ) {
    init {
        on<MultiAddEvent> { event, emit ->
            val s = state
            val newcounter= s.counter.clone()
            newcounter[event.counterIdx] += event.value
            emit(s.copy(counter = newcounter))
        }
        on<MultiSubEvent> { event, emit ->
            val s = state
            val newcounter= s.counter.clone()
            newcounter[event.counterIdx] -= event.value
            emit(s.copy(counter = newcounter))
        }
    }

}