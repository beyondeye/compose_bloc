package com.beyondeye.kbloc.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


/**
 * An [Emitter] is a class which is capable of emitting new states.
 *
 * See also:
 *
 * * [EventHandler] which has access to an [Emitter].
 */
public interface Emitter<State> {
    /**
     * Subscribes to the provided [stream] and invokes the [onData] callback
     * when the [stream] emits new data.
     *
     * [onEach] completes when the event handler is cancelled or when
     * the provided [stream] has ended.
     *
     * If [onError] is omitted, any errors on this [stream]
     * are considered unhandled, and will be thrown by [onEach].
     * As a result, the internal subscription to the [stream] will be canceled.
     *
     * If [onError] is provided, any errors on this [stream] will be passed on to
     * [onError] and will not result in unhandled exceptions or cancelations to
     * the internal stream subscription.
     *
     *
     */
    public suspend fun <T> onEach(
        stream: Flow<T>,
        onData: (data: T) -> Unit,
        onError: ((error: Throwable) -> Unit)? = null
    ): Job

    /**
     * Subscribes to the provided [stream] and invokes the [onData] callback
     * when the [stream] emits new data and the result of [onData] is emitted.
     *
     * [forEach] completes when the event handler is cancelled or when
     * the provided [stream] has ended.
     *
     * If [onError] is omitted, any errors on this [stream]
     * are considered unhandled, and will be thrown by [forEach].
     * As a result, the internal subscription to the [stream] will be canceled.
     *
     * If [onError] is provided, any errors on this [stream] will be passed on to
     * [onError] and will not result in unhandled exceptions or cancelations to
     * the internal stream subscription.
     */
    public suspend fun <T> forEach(
        stream: Flow<T>, onData: (data: T) -> State,
        onError: ((error: Throwable) -> State)? = null
    ): Job

    /**
     * Whether the [EventHandler] associated with this [Emitter]
     * has been completed or canceled.
     */
    public fun isDone(): Boolean

    /**
     * Emits the provided [state].
     */
    public fun call(state: State)

    public operator fun invoke(state: State) {
        call(state)
    }

}

@PublishedApi
internal class _Emitter<State>(private val _emit: (State) -> Unit,private val _cscope:CoroutineScope=GlobalScope) : Emitter<State> {
    private var _isCanceled = false
    private var _isCompleted = false
    private val _completer = CompletableDeferred<Unit>()
    private val _disposables = mutableListOf<Job>()
    override suspend fun <T> onEach(
        stream: Flow<T>,
        onData: (data: T) -> Unit,
        onError: ((error: Throwable) -> Unit)?
    ): Job {
        val completer = CompletableDeferred<Unit>()
        val stream_w_attached_callbacks = stream.cancellable().catch {
            if (onError != null) {
                onError(it)
            } else {
                completer.completeExceptionally(it)
            }
        }.onEach {
            onData(it)
        }.onCompletion {
            completer.complete(Unit)
        }
        //cancelOnError: onError == null, TODO how to implement this?
        val subscription = coroutineScope { launch { stream_w_attached_callbacks.collect() } }
        _disposables.add(subscription)
        return _cscope.launch {
            listOf(future, completer).awaitAny(this).await()
            try {
                subscription.cancel()
            } finally {
                _disposables.remove(subscription)
            }
        }
    }

    override suspend fun <T> forEach(
        stream: Flow<T>,
        onData: (data: T) -> State,
        onError: ((error: Throwable) -> State)?
    ): Job {
        return onEach(
            stream,
            onData = { data: T -> call(onData(data)) },
            onError = if (onError != null) { error: Throwable -> call(onError(error)) } else null
        )
    }

    override fun isDone(): Boolean {
        return _isCanceled || _isCompleted
    }
    val notAwaitedJobInEventHandlerErrorMsg =
"""
\n\n
emit was called after an event handler completed normally.
This is usually due to an unawaited future in an event handler.
Please make sure to await all asynchronous operations with event handlers
and use emit.isDone after asynchronous operations before calling emit() to
ensure the event handler has not completed.

  **BAD**
  on<Event>{ event, emit-> 
    async {
        //..some async ops
        emit(...)
    }
  }

  **GOOD**
  on<Event>{ event, emit-> 
    val deferred=async {
        //..some async ops
        emit(...)
    }
    deferred.await()
  }
"""
    override fun call(state: State) {
        //TODO throw error instead of assert?
        if(_isCompleted) throw Exception(notAwaitedJobInEventHandlerErrorMsg)
        if (!_isCanceled) _emit(state)
    }

    fun cancel() {
        if (isDone()) return
        _isCanceled = true
        _close()
    }
    val pendingSubscriptionsErrorMsg = """
\n\n
An event handler completed but left pending subscriptions behind.
This is most likely due to an unawaited emit.forEach or emit.onEach.
Please make sure to await all asynchronous operations within event handlers.

  **BAD**
  on<Event>{ event, emit ->
    emit.forEach(...)
  }

  **GOOD**
  on<Event>{ event, emit ->
    val job=emit.forEach(...)
    job.join()
  }
"""

    fun complete() {
        if (isDone()) return
        //TODO throw error instead of assert?
        if(!_disposables.isEmpty()) {
            throw Exception(pendingSubscriptionsErrorMsg)
        }
        _isCompleted = true
        _close()
    }

    fun _close() {
        for (disposable in _disposables) {
            disposable.cancel()
        }
        _disposables.clear()
        if (!_completer.isCompleted) _completer.complete(Unit)
    }

    val future: CompletableDeferred<Unit> get() = _completer

}

