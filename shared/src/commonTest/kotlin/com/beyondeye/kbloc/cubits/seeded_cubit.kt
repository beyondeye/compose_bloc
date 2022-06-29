package com.beyondeye.kbloc.cubits

import com.beyondeye.kbloc.core.Cubit
import kotlinx.coroutines.GlobalScope

class SeededCubit<T:Any>(initialState:T): Cubit<T>(initialState,GlobalScope,false)
{
    fun emitState(state:T) {
        emit(state)
    }
}
