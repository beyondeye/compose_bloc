package com.beyondeye.kbloc.concurrency

import com.beyondeye.kbloc.core.EventTransformer
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapConcat

public fun  <Event>EventTransformer_debounce(duration:Long):EventTransformer<Event> ={ events, mapper ->
    events.debounce(duration).flatMapConcat(mapper);
}
