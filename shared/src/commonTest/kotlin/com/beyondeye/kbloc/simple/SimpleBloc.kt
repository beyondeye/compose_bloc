package com.beyondeye.kbloc.simple

import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.on
import kotlinx.coroutines.CoroutineScope

class SimpleBloc :Bloc<Any,String> {
    constructor(cscope_stateUpdate: CoroutineScope,
                useReferenceEqualityForStateChanges: Boolean
    ) : super("", cscope_stateUpdate, useReferenceEqualityForStateChanges) {
        on<String, Any, String>(handler = { _, emit ->
            emit("data")
        })
    }
}
/*
class SimpleBloc extends Bloc<dynamic, String> {
    SimpleBloc() : super('') {
        on<String>((_, emit) => emit('data'));
    }
}

 */
