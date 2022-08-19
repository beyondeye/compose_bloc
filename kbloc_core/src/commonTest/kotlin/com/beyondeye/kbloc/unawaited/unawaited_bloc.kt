package com.beyondeye.kbloc.unawaited

import com.beyondeye.kbloc.core.Bloc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

class UnawaitedEvent

class UnawaitedState

class UnawaitedBloc(cscope:CoroutineScope) :
    Bloc<UnawaitedEvent,UnawaitedState>(cscope, UnawaitedState(),
        false,false) {
    constructor(cscope: CoroutineScope,future:Deferred<Unit>):this(cscope) {
        on<UnawaitedEvent>{ event, emit ->
            //TODO the original code is
            future.invokeOnCompletion { emit(UnawaitedState()) }
        }
    }
}

