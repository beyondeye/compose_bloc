package com.beyondeye.kbloc_app

import com.beyondeye.kbloc.core.Bloc
import kotlinx.coroutines.CoroutineScope

interface CounterEvent
object IncrementEvent:CounterEvent
object DecrementEvent:CounterEvent
class AdditionEvent(val value:Int):CounterEvent
class SubtractionEvent(val value:Int):CounterEvent
data class CounterState(val counter:Int=0)

class CounterBloc(cscope: CoroutineScope, startCounter:Int=0): Bloc<CounterEvent, CounterState>(cscope,
    CounterState(startCounter),false,true
) {
    init {
        on<IncrementEvent> { event, emit ->
            val s=state
            emit(s.copy(counter =s.counter+1 ))
        }
        on<AdditionEvent> { event, emit ->
            val s=state
            emit(s.copy(counter =s.counter+event.value ))
        }
        on<DecrementEvent> { event, emit ->
            val s=state
            emit(s.copy(counter =s.counter-1 ))
        }
        on<SubtractionEvent> { event, emit ->
            val s=state
            emit(s.copy(counter =s.counter-event.value ))
        }
    }

}