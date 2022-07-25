package com.beyondeye.kbloc.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

public interface DeferredStateFlow<out T> : SharedFlow<T> {
    /**
     * The current value of this state flow: it is a deferred value so you must wait for it
     */
    public val value: Deferred<T>
}


/**
 * a [MutableStateFlow] where the state cannot be updated directly by writing to it, but instead by calling
 * [queueStateUpdate] method
 */
public class MutableDeferredStateFlow<S:Any>(initialValue:S,
                                  public val cscope_stateUpdate: CoroutineScope):DeferredStateFlow<S> {
    private val _stateFlow = MutableStateFlow(initialValue)

    private var deferredState: Deferred<S> = cscope_stateUpdate.async { initialValue }

    /**
     * the purpose of this method is to make sure that when dispatching multiple events from the same coroutine
     * the updates are processed in the order that they are queued
     * this is also apparently the behavior of StreamController<State>.broadcast() used in the dart version of bloc
     * @return a [Deferred] with the value of the updated state
     */
    public fun queueStateUpdate( updatefn:suspend (S)->S, useRefEqualityCheck:Boolean=true):Deferred<S> {
        val curdeferred=deferredState
        deferredState=cscope_stateUpdate.async {
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
        return deferredState
    }

    override val replayCache: List<S>
        get() = _stateFlow.replayCache

    override suspend fun collect(collector: FlowCollector<S>): Nothing {
        _stateFlow.collect(collector)
    }

    override val value: Deferred<S>
        get() = deferredState
}
