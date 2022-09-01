package screens

import com.beyondeye.kbloc.core.Cubit
import kotlinx.coroutines.CoroutineScope

class CounterCubit(cscope: CoroutineScope, startCounter:Int=0): Cubit<CounterState>(
    cscope,CounterState(startCounter),true
) {
    fun increment() {
        queueStateUpdate { it.copy(counter =it.counter+1) }
    }
    fun decrement() {
        queueStateUpdate { it.copy(counter = it.counter-1) }
    }
    fun addition(value:Int) {
        queueStateUpdate { it.copy(counter = it.counter+value) }
    }
    fun subtraction(value:Int) {
        queueStateUpdate { it.copy(counter = it.counter-value) }
    }
}