package com.beyondeye.kbloc.seeded

import com.beyondeye.kbloc.core.Bloc
import kotlinx.coroutines.CoroutineScope

class SeededBloc (
    val seed:Int, val states:List<Int>,
    cscope_stateUpdate: CoroutineScope, useReferenceEqualityForStateChanges: Boolean=false) :
    Bloc<String, Int>(cscope_stateUpdate, seed, useReferenceEqualityForStateChanges,false) {
    init {
        on<String>{ event, emit ->
            for (state in states) {
                emit(state)
            }
        }
    }
}
