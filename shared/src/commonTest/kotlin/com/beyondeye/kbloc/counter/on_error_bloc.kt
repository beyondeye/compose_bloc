package com.beyondeye.kbloc.counter

import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.Emitter
import kotlinx.coroutines.CoroutineScope

class OnErrorBloc(cscope: CoroutineScope,
                  val error: Throwable,
                  val onErrorCallback: (Throwable) -> Unit
) : Bloc<CounterEvent, Int>(cscope, 0) {
    init {
        on<CounterEvent>(handler = ::_onCounterEvent)
    }

    override fun onError(error: Throwable) {
        onErrorCallback(error)
        super.onError(error)
    }

    fun _onCounterEvent(event: CounterEvent, emit: Emitter<Int>) {
        throw error
    }
}