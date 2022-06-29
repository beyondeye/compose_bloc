package com.beyondeye.kbloc.cubits

import com.beyondeye.kbloc.core.Cubit
import kotlinx.coroutines.GlobalScope

class FakeAsyncCounterCubit: Cubit<Int>(0,GlobalScope,false) {
    suspend fun increment() {
        val nextState= _increment(state)
        emit(nextState)
    }
    suspend private fun _increment(value:Int): Int {
        return value+1
    }
}

