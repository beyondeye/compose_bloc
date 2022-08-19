package com.beyondeye.kbloc.counter

import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.Emitter
import kotlinx.coroutines.CoroutineScope

class OnEventErrorBloc(cscope: CoroutineScope,
                  val exception: Exception,
) : Bloc<CounterEvent, Int>(cscope, 0,false,false) {
    init {
        on<CounterEvent> { _,_-> }
    }

    override fun onEvent(event: CounterEvent) {
        throw exception
        //not calling super intentionally! in any case it will be ignored because we throw exception
        //super.onEvent(event)
    }
}
