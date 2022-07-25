package com.beyondeye.kbloc.async

import com.beyondeye.kbloc.core.Bloc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge

class AsyncBloc(cscope: CoroutineScope) : Bloc<AsyncEvent,AsyncState>(cscope,AsyncState.initial(),false)
{
    init {
        on<AsyncEvent>(transformer = { events,mapper-> events.asyncExpand(mapper) }) { event,emit ->
            emit(state.copy(isLoading = true, isSuccess = false))
            delay(0)
            emit(state.copy(isLoading = false, isSuccess = true))
        }
    }
}

/**
* Transforms each element into a sequence of asynchronous events.
*
* Returns a new stream and for each event of this stream, do the following:
*
* * If the event is an error event or a done event, it is emitted directly
* by the returned stream.
* * Otherwise it is an element. Then the [convert] function is called
* with the element as argument to produce a convert-stream for the element.
* * If that call throws, the error is emitted on the returned stream.
* * If the call returns `null`, no further action is taken for the elements.
* * Otherwise, this stream is paused and convert-stream is listened to.
* Every data and error event of the convert-stream is emitted on the returned
* stream in the order it is produced.
* When the convert-stream ends, this stream is resumed.
*
* The returned stream is a broadcast stream if this stream is.
 */
public fun <E,T> Flow<T>.asyncExpand(convert: (T) -> Flow<E>): Flow<E> {
    //see also https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flat-map-concat.html
    return flatMapConcat(convert)
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
public fun <T> Flow<T>.concurrentAsyncExpand(mapper: (T) -> Flow<T>): Flow<T> {
    //see also https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flat-map-merge.html
    return flatMapMerge(transform = mapper)
}


/*
    DARIO: CODE EXTRACTED from stream.dart

  Stream<E> asyncExpand<E>(Stream<E>? convert(T event)) {
    _StreamControllerBase<E> controller;
    if (isBroadcast) {
      controller = _SyncBroadcastStreamController<E>(null, null);
    } else {
      controller = _SyncStreamController<E>(null, null, null, null);
    }

    controller.onListen = () {
      StreamSubscription<T> subscription = this.listen(null,
          onError: controller._addError, // Avoid Zone error replacement.
          onDone: controller.close);
      subscription.onData((T event) {
        Stream<E>? newStream;
        try {
          newStream = convert(event);
        } catch (e, s) {
          controller.addError(e, s);
          return;
        }
        if (newStream != null) {
          subscription.pause();
          controller.addStream(newStream).whenComplete(subscription.resume);
        }
      });
      controller.onCancel = subscription.cancel;
      if (!isBroadcast) {
        controller
          ..onPause = subscription.pause
          ..onResume = subscription.resume;
      }
    };
    return controller.stream;
  }
 */