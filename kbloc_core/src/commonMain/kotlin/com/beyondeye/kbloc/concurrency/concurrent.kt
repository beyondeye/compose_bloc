package com.beyondeye.kbloc.concurrency

import com.beyondeye.kbloc.core.EventTransformer
import com.beyondeye.kbloc.core.concurrentAsyncExpand

/**
 * Process events concurrently.
 *
 * **Note**: there may be event handler overlap and state changes will occur
 * as soon as they are emitted. This means that states may be emitted in
 * an order that does not match the order in which the corresponding events
 * were added.
 */
public fun <Event>EventTransformer_concurrent():EventTransformer<Event> = { events, mapper ->
    events.concurrentAsyncExpand(mapper)
}
