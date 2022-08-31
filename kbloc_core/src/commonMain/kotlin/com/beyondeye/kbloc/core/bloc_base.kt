package com.beyondeye.kbloc.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

public class StateError(msg: String) : Exception(msg)

/**
 * An object that provides access to a stream of states over time.
 */
public interface Streamable<State : Any?> {
    /**
     * The current [stream] of states.
     */
    public val stream: Flow<State>
}

/**
 * A [Streamable] that provides synchronous access to the current [state].
 */
public interface StateStreamable<State> : Streamable<State> {
    /**
     * The current [state].
     */
    public val state: State
}

/**
 * An object that must be closed when no longer in use.
 */
public interface Closable {
    /**
     * Closes the current instance.
     */
    public suspend fun close()

    /**
     * Whether the object is closed.
     *
     * An object is considered closed once [close] is called.
     */
    public val isClosed: Boolean
}

/**
 * A [StateStreamable] that must be closed when no longer in use.
 */
public interface StateStreamableSource<State> : StateStreamable<State>, Closable

/**
 * An object that can emit new states.
 */
public interface Emittable<State : Any?> {
    /**
     * Emits a new [state].
     * NOTE: don't use this method for asynchronous state updates, use instead [queueStateUpdate]
     */
    public fun emit(state: State)

    /**
     * use this method if you need update the state from an async method.
     * usinq [queueStateUpdate] will garantee that multiple async state updates will be
     * executed in the same order as they were queued
     * THIS METHOD IS NOT PRESENT IN THE ORIGINAL dart bloc implementation
     * @return deferred of the updated stated
     */
    public fun queueStateUpdate(stateUpdateFun: suspend (curState: State) -> State): Deferred<State>
}

/**
 * A generic destination for errors.
 *
 * Multiple errors can be reported to the sink via `addError`.
 */
public interface ErrorSink : Closable {
    /**
     * Adds an [error] to the sink
     *
     * Must not be called on a closed sink.
     * You can get stack trace (if available) with error.stackTraceToString()
     */
    public fun addError(error: Throwable)
}

/** An interface for the core functionality implemented by
 * both [Bloc] and [Cubit].
 */
public abstract class BlocBase<State : Any>// ignore: invalid_use_of_protected_member
/**
 * the coroutine scope used for running async state update function (queueStateUpdate)
 * and suspend functions in event handlers
 */ public constructor(
    initialState: State,
    /**
     * the coroutine scope used for running async state update function (queueStateUpdate)
     * and suspend functions in event handlers
     */
    public val cscope: CoroutineScope, useReferenceEqualityForStateChanges: Boolean
) : StateStreamableSource<State>, Emittable<State>, ErrorSink {
    @PublishedApi
    internal var _emitted: Boolean = false

    @PublishedApi
    internal val _useReferenceEqualityForStateChanges: Boolean = useReferenceEqualityForStateChanges

    //TODO: should not I will busing get() here, so that I always get the most updated version of current?
    protected val _blocObserver: BlocObserver<Any>? = BlocOverrides.current?.blocObserver

    /**
     * see https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/index.html
     */
    private val _stateController: MutableDeferredStateFlow<State>
    //
    public override val state: State get() = _stateController.value
    public override val stream: StateFlow<State> get() = _stateController

    private var _isClosed: Boolean = false

    /**
     *  Whether the bloc is closed.
     *
     *  A bloc is considered closed once [close] is called.
     *  Subsequent state changes cannot occur within a closed bloc.
     */
    public override val isClosed: Boolean get() = _isClosed

    /** Updates the [state] field to the provided [state].
     *  [emit] does nothing if the [state] being emitted
     *  is equal to the current [state].
     *
     * NOTE: In the original DART implementation equality between old state and new state  is always checked by value.
     *
     *  To allow for the possibility of notifying listeners of the initial state,
     *  emitting a state which is equal to the initial state is allowed as long
     *  as it is the first thing emitted by the instance.
     *
     *  * Throws a [StateError] if the bloc is closed.
     *  *DARIO* avoid using [emit] and use instead [queueStateUpdate] that avoid race conditions between
     *          multiple parallel state updates
     */
    override fun emit(state: State) {
        cscope.async {
            try {
                if (isClosed) {
                    throw StateError("Cannot emit new states after calling close")
                }
                //TODO: using equal here is expensive: perhaps remove it?
                val curStateDeferred = _stateController.valueDeferred
                val updatedState = _stateController.queueStateUpdate(
                    { state },
                    _useReferenceEqualityForStateChanges
                ).await()
                val curState = curStateDeferred.await()
                val notchanged =
                    if (_useReferenceEqualityForStateChanges) curState === updatedState else curState == updatedState
                //TODO: currently the behavior of the stream of states is different from dart implementation: in dart the initial state
                //   is optional, in kotlin an initial state is required: I suspect that this difference also means that the logic here that
                //   make use of the _emitted flag is no more needed: need to decide on this
                if (notchanged && _emitted) return@async
                onChange(Change(curState, updatedState))
                _emitted = true
            } catch (error: Throwable) {
                onError(error)
                throw error
            }
        }
    }

    override fun queueStateUpdate(stateUpdateFun: suspend (curState: State) -> State): Deferred<State> {
        return cscope.async {
            //note that exceptions thrown here will not bubble up unless the call await() on the returned Deferred object
            val updatedState: State
            try {
                if (isClosed) {
                    throw StateError("Cannot emit new states after calling close")
                }
                //TODO: using equal here is expensive: perhaps remove it?
                val curStateDeferred = _stateController.valueDeferred
                updatedState = _stateController.queueStateUpdate(
                    stateUpdateFun,
                    _useReferenceEqualityForStateChanges
                ).await()
                val curState = curStateDeferred.await()
                val notchanged =
                    if (_useReferenceEqualityForStateChanges) curState === updatedState else curState == updatedState
                //TODO: currently the behavior of the stream of states is different from dart implementation: in dart the initial state
                //   is optional, in kotlin an initial state is required: I suspect that this difference also means that the logic here that
                //   make use of the _emitted flag is no more needed: need to decide on this
                if (notchanged && _emitted) return@async curState
                onChange(Change(curState, updatedState))
                _emitted = true
            } catch (error: Throwable) {
                onError(error)
                throw error
            }
            updatedState
        }
    }

    /** Called whenever a [change] occurs with the given [change].
     *  A [change] occurs when a new `state` is emitted.
     *  [onChange] is called after the `state` of the `cubit` is updated.
     *  NOTE THAT IN THE ORIGINAL DART IMPLEMENTATION [onChange] is called BEFORE the state is updated
     *  [onChange] is a great spot to add logging/analytics for a specific `cubit`.
     *
     *  **Note: `super.onChange` should always be called first.**
     *  override fun onChange(change:Change) {
     *    // Always call super.onChange with the current change
     *    super.onChange(change)
     *
     *    // Custom onChange logic goes here
     *    //...
     *  }
     *
     *  See also:
     *
     *  * [BlocObserver] for observing [Cubit] behavior globally.
     *
     */
    @MustCallSuper
    protected open fun onChange(change: Change<State>) {
        // ignore: invalid_use_of_protected_member
        _blocObserver?.onChange(this as BlocBase<Any>, change as Change<Any>)
    }

    /**
     * Reports an [error] which triggers [onError]
     */
    @MustCallSuper
    override fun addError(error: Throwable) {
        onError(error)
    }


    /** Called whenever an [error] occurs and notifies [BlocObserver.onError].
     *
     *  **Important Note: `super.onError` should always be called last.**
     *
     *  override fun onError(error:Throwable) {
     *    // Custom onError logic goes here
     *    //  ...
     *    // Always call super.onError with the current error and stackTrace
     *    super.onError(error)
     *  }
     *  ```
     */
    @MustCallSuper
    protected open fun onError(error: Throwable) {
        // ignore: invalid_use_of_protected_member
        _blocObserver?.onError(this as BlocBase<Any>, error)
    }

    /**
     * Closes the instance.
     * This method should be called when the instance is no longer needed.
     * Once [close] is called, the instance can no longer be used.
     */
    @MustCallSuper
    override suspend fun close() {
        _blocObserver?.onClose(this as BlocBase<Any>)
        _isClosed = true
    }

    /**
     * *DARIO* combine [close] and [cscope].cancel()
     */
    public suspend fun dispose() {
        close()
        cscope.cancel()
    }

    init {
        _stateController = MutableDeferredStateFlow(initialState, cscope)
        _blocObserver?.onCreate(this as BlocBase<Any>)
    }
}
