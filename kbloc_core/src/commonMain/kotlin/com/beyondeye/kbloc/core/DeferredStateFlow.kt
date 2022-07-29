package com.beyondeye.kbloc.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public interface DeferredStateFlow<out T> : StateFlow<T> {
    /**
     * The current value of this state flow: it is a deferred value so you must wait for it:
     * it is useful if you want to wait for some state update operation to complete before reading the state value
     * for example as when we update the state using
     * if you simply want to get the current state without waiting you can simply use the [value] property
     */
    public val valueDeferred: Deferred<T>
}


/**
 * a [MutableStateFlow] where the state cannot be updated directly by writing to it, but instead by calling
 * [queueStateUpdate] method
 */
public class MutableDeferredStateFlow<S:Any>(initialValue:S,
                                  public val cscope_stateUpdate: CoroutineScope):DeferredStateFlow<S> {
    private val _stateFlow = MutableStateFlow(initialValue)

    private var _deferredState: Deferred<S> = cscope_stateUpdate.async { initialValue }

    /**
     * the purpose of this method is to make sure that when dispatching multiple events from the same coroutine
     * the updates are processed in the order that they are queued
     * this is also apparently the behavior of StreamController<State>.broadcast() used in the dart version of bloc
     * @return a [Deferred] with the value of the updated state
     * TODO: another mechanism that could be potentially used instead of [queueStateUpdate] is [MutableStateFlow.updateAndGet]
     * https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/update-and-get.html
     */
    public fun queueStateUpdate( updatefn:suspend (S)->S, useRefEqualityCheck:Boolean=true):Deferred<S> {
        val curdeferred=_deferredState
        _deferredState=cscope_stateUpdate.async {
            val curState=curdeferred.await()
            val newState=updatefn(curState)
            if(useRefEqualityCheck && curState===newState)
                curState
            else {
                //implementation of MutableStateFlow will check if curState==newState and in such case will leave
                //_stateFlow.value unchanged
                //TODO make a forked version of StateFlow that optionally use reference equality to check for changed state
                //
                _stateFlow.value = newState
                newState
            }
        }
        return _deferredState
    }

    override val replayCache: List<S>
        get() = _stateFlow.replayCache

    override suspend fun collect(collector: FlowCollector<S>): Nothing {
        _stateFlow.collect(collector)
    }

    override val valueDeferred: Deferred<S>
        get() = _deferredState

    /**
     * this the current value of the flow. if a [queueStateUpdate] operation was started, this
     * value is stale (not up-to-date). better always read the current value using [valueDeferred]
     */
    override val value: S
        get() = _stateFlow.value
}
