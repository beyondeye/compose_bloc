package com.beyondeye.kbloc.concurrency

import com.beyondeye.kbloc.core.EventTransformer
import com.beyondeye.kbloc.core.asyncExpand

/**
 * Process events one at a time by maintaining a queue of added events
 * and processing the events sequentially.
 *
 * **Note**: there is no event handler overlap and every event is guaranteed
 * to be handled in the order it was received.
 */
public fun <Event>EventTransformer_sequential():EventTransformer<Event> = { events, mapper ->
    events.asyncExpand(mapper)
}