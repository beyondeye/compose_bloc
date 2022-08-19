package com.beyondeye.kbloc.complex

import com.beyondeye.kbloc.core.Bloc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration

const private val _delay_ms:Long =100

class ComplexBloc(cscope:CoroutineScope) : Bloc<ComplexEvent, ComplexState>(cscope,ComplexStateA(),false,false)
{
    init {
        on<ComplexEventA> { _,emit ->
            emit(ComplexStateA())
        }
        on<ComplexEventB> {_,emit ->
            emit(ComplexStateB())
        }
        on<ComplexEventC> { _, emit ->
            delay(_delay_ms)
            emit(ComplexStateC())
        }
        on<ComplexEventD> { _, emit ->
            delay(_delay_ms)
            emit(ComplexStateD())
        }
    }

    override val stream: StateFlow<ComplexState>
        get() = super.stream.debounce(50).stateIn(cscope)
 }
