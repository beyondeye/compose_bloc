package com.beyondeye.kbloc.core

import kotlinx.coroutines.CoroutineScope


/**
 * A [Cubit] is similar to [Bloc] but has no notion of events
 * and relies on methods to [emit] new states.
 *
 * Every [Cubit] requires an initial state which will be the
 * state of the [Cubit] before [emit] has been called.
 *
 * The current state of a [Cubit] can be accessed via the [state] getter.
 *
 * ```kotlin
 * class CounterCubit : Cubit<int> {
 *  CounterCubit() : super(0);
 *
 *   void increment() => emit(state + 1);
 * }
 * ```
 */
public abstract class Cubit<State:Any>(
    /**
     * the coroutine scope used for running async state update function (queueStateUpdate)
     * and suspend functions in event handlers
     */
    cscope: CoroutineScope,
    initialState: State,
    useReferenceEqualityForStateChanges: Boolean
) : BlocBase<State>(initialState,cscope,useReferenceEqualityForStateChanges)
{

}
