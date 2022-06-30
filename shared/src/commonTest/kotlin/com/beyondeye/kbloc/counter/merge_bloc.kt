package com.beyondeye.kbloc.counter

import com.beyondeye.kbloc.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.merge

val customTransformer:EventTransformer<CounterEvent> = { events, mapper ->
    val nonDebounceStream = events.filter { it !=CounterEvent.increment }
    // about the throttle operator in rxjava and kotlin, see
    //https://proandroiddev.com/from-rxjava-to-kotlin-flow-throttling-ed1778847619
    val debounceStream = events.filter { it == CounterEvent.increment }.throttleFirst(100)

    merge(nonDebounceStream,debounceStream).concurrentAsyncExpand(mapper)
}

/// Like [asyncExpand] but the [convert] callback may be called for an element
/// before the [Stream] emitted by the previous element has closed.
///
/// Events on the result stream will be emitted in the order they are emitted
/// by the sub streams, which may not match the order of the original stream.
///
/// Errors from [convert], the source stream, or any of the sub streams are
/// forwarded to the result stream.
///
/// The result stream will not close until the source stream closes and all
/// sub streams have closed.
///
/// If the source stream is a broadcast stream, the result will be as well,
/// regardless of the types of streams created by [convert]. In this case,
/// some care should be taken:
/// -  If [convert] returns a single subscription stream it may be listened to
/// and never canceled.
/// -  For any period of time where there are no listeners on the result
/// stream, any sub streams from previously emitted events will be ignored,
/// regardless of whether they emit further events after a listener is added
/// back.
///
/// See also:
///
///  * [switchMap], which cancels subscriptions to the previous sub
///    stream instead of concurrently emitting events from all sub streams.
private fun <T> Flow<T>.concurrentAsyncExpand(mapper: (T) -> Flow<T>): Flow<T> {

    TODO()
}

class MergeBloc(cscope:CoroutineScope,val  onTransitionCallback: onTransitionCallback?=null) : Bloc<CounterEvent, Int>(cscope,0) {
    init {
        on<CounterEvent>(transformer = customTransformer, handler = ::_onCounterEvent)
    }

    override fun onTransition(transition: Transition<CounterEvent, Int>) {
        super.onTransition(transition)
        onTransitionCallback?.invoke(transition)
    }

    fun _onCounterEvent(event:CounterEvent,emit:Emitter<Int>) {
        when(event) {
            CounterEvent.increment -> emit(state + 1)
            CounterEvent.decrement -> emit( state -1)
        }
    }
}
