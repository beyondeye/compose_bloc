package com.beyondeye.kbloc_app

import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.Emitter
import kotlinx.coroutines.CoroutineScope

data class ABCCounterState(val a:Int,val b:Int, val c:Int)

class ABCCounterBloc(
    cscope: CoroutineScope,
    startCounters: Array<Int> = Array(3) { 0 }
) :
    Bloc<MultiCounterEvent,
            ABCCounterState>(
        cscope, ABCCounterState(a= startCounters[0], b=startCounters[1],c=startCounters[2]),
        true,
        true
    ) {
    init {
        on<MultiAddEvent> { event, emit ->
            handleAddEvent(state, emit, event.counterIdx, event.value)
        }
        on<MultiSubEvent> { event, emit ->
            handleAddEvent(state, emit, event.counterIdx, -event.value)
        }
    }
    companion object {
        private fun handleAddEvent(
            s: ABCCounterState,
            emit: Emitter<ABCCounterState>,
            idx: Int,
            delta: Int
        ) {
            val newState = when (idx) {
                0 -> s.copy(a = s.a + delta)
                1 -> s.copy(b = s.b + delta)
                2 -> s.copy(c = s.c + delta)
                else -> throw NotImplementedError()
            }
            emit(newState)
        }
    }

}