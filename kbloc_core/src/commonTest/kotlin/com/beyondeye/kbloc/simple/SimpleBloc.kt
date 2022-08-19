package com.beyondeye.kbloc.simple

import com.beyondeye.kbloc.core.Bloc
import kotlinx.coroutines.CoroutineScope

class SimpleBloc (cscope_stateUpdate: CoroutineScope, useReferenceEqualityForStateChanges: Boolean=false) :
    Bloc<Any, String>(cscope_stateUpdate, "", useReferenceEqualityForStateChanges,false) {
    init {
        on<String>{ _, emit ->
            emit("data")
        }
    }
}

