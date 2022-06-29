package com.beyondeye.kbloc.seeded

import com.beyondeye.kbloc.core.Bloc
import kotlinx.coroutines.CoroutineScope

class SeededBloc (
    val seed:Int, val states:List<Int>,
    cscope_stateUpdate: CoroutineScope, useReferenceEqualityForStateChanges: Boolean) :
    Bloc<String, Int>(seed, cscope_stateUpdate, useReferenceEqualityForStateChanges) {
    init {
        on<String>{ event, emitter ->
            //TODO: the original DART code is on<String>((_, emit) => emit('data'));
            //   that apparently don't use the emit parameter, but instead use the bloc.emit() method: this is potentially an error
            //need to understand better
            for (state in states) {
//                emit(state);
                emitter.call(state)
            }
        }
    }
}
