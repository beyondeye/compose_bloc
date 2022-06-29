package com.beyondeye.kbloc.simple

import com.beyondeye.kbloc.core.Bloc
import kotlinx.coroutines.CoroutineScope

class SimpleBloc :Bloc<Any,String> {
    constructor(cscope_stateUpdate: CoroutineScope,
                useReferenceEqualityForStateChanges: Boolean
    ) : super("", cscope_stateUpdate, useReferenceEqualityForStateChanges) {
        on<String>{ _, emitter ->
            //TODO: the original DART code is on<String>((_, emit) => emit('data'));
            //   that apparently don't use the emit parameter, but instead use the bloc.emit() method: this is potentially an error
            //need to understand better
//            emit("data")
            emitter.call("data")
        }
    }
}

