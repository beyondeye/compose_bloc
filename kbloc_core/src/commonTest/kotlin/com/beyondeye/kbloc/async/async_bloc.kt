package com.beyondeye.kbloc.async

import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.asyncExpand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge

class AsyncBloc(cscope: CoroutineScope) : Bloc<AsyncEvent,AsyncState>(cscope,AsyncState.initial(),false,false)
{
    init {
        on<AsyncEvent>(transformer = { events,mapper-> events.asyncExpand(mapper) }) { event,emit ->
            emit(state.copy(isLoading = true, isSuccess = false))
            delay(0)
            emit(state.copy(isLoading = false, isSuccess = true))
        }
    }
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