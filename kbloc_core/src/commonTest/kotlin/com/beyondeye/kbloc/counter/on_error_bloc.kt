package com.beyondeye.kbloc.counter

import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.Emitter
import kotlinx.coroutines.CoroutineScope

/**
 * note: in kotlin there is not  much difference between Error and Exception so
 * [OnErrorBloc] and [OnExceptionBloc] are basically the same
 */
class OnErrorBloc(cscope: CoroutineScope,
                  val error: Throwable,
                  val onErrorCallback: (Throwable) -> Unit
) : Bloc<CounterEvent, Int>(cscope, 0,false,false) {
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