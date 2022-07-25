package com.beyondeye.kbloc.cubits

import com.beyondeye.kbloc.core.Change
import com.beyondeye.kbloc.core.Cubit
import kotlinx.coroutines.GlobalScope

class CounterCubit(
    val onChangeCallback:((Change<Int>)->Unit)?=null,
    val onErrorCallback:((Throwable)->Unit)?=null
) :Cubit<Int>(0,GlobalScope,false) {
    fun increment() {
        emit(state+1)
    }
    fun decrement() {
        emit(state-1)
    }

    override fun onChange(change: Change<Int>) {
        super.onChange(change)
        onChangeCallback?.invoke(change)
    }

    override fun onError(error: Throwable) {
        onErrorCallback?.invoke(error)
        super.onError(error)
    }
}
