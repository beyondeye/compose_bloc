package com.beyondeye.kbloc.simple

import com.beyondeye.kbloc.core.Bloc
import kotlinx.coroutines.CoroutineScope

class SimpleBloc (cscope_stateUpdate: CoroutineScope, useReferenceEqualityForStateChanges: Boolean) :
    Bloc<Any, String>(cscope_stateUpdate, "", useReferenceEqualityForStateChanges) {
    init {
        on<String>{ _, emit ->
            emit("data")
        }
    }
}

