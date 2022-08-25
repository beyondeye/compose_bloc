package cafe.adriel.voyager.core.concurrent

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate

//using AtomicInt32 is not really necessary: we use atomic from atomicfu directly
//public actual class AtomicInt32 actual constructor(initialValue: Int) {
//    private var _value: AtomicInt = atomic(initialValue)
//    public actual fun getAndIncrement(): Int {
//        return _value.getAndIncrement()
//    }
//}

