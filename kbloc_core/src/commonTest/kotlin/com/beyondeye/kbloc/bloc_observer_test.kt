package com.beyondeye.kbloc

import com.beyondeye.kbloc.core.*
import com.beyondeye.kbloc.counter.CounterEvent
import kotlinx.coroutines.GlobalScope
import kotlin.test.Test

class DefaultBlocObserver :BlocObserver<Any> {}

class BlocObserverTest
{
    @Test
    fun OnCreate_does_nothing_by_default() {
        val bloc=CounterBloc(GlobalScope)
        val error=Exception()
        val event = CounterEvent.increment
        val change = Change(0,1)
        val transition = Transition(1,CounterEvent.increment,1)

        DefaultBlocObserver().onCreate(bloc as Bloc<Any,Any>)
    }

    @Test
    fun OnEvent_does_nothing_by_default() {
        val bloc=CounterBloc(GlobalScope)
        val error=Exception()
        val event = CounterEvent.increment
        val change = Change(0,1)
        val transition = Transition(1,CounterEvent.increment,1)

        DefaultBlocObserver().onEvent(bloc  as Bloc<Any,Any>,event as Any)
    }

    @Test
    fun OnChange_does_nothing_by_default() {
        val bloc=CounterBloc(GlobalScope)
        val error=Exception()
        val event = CounterEvent.increment
        val change = Change(0,1)
        val transition = Transition(1,CounterEvent.increment,1)

        DefaultBlocObserver().onChange(bloc  as Bloc<Any,Any>,change as Change<Any>)
    }

    @Test
    fun OnTransition_does_nothing_by_default() {
        val bloc=CounterBloc(GlobalScope)
        val error=Exception()
        val event = CounterEvent.increment
        val change = Change(0,1)
        val transition = Transition(1,CounterEvent.increment,1)

        DefaultBlocObserver().onTransition(bloc  as Bloc<Any,Any>,transition as Transition<Any?, Any>)
    }

    @Test
    fun OnError_does_nothing_by_default() {
        val bloc=CounterBloc(GlobalScope)
        val error=Exception()
        val event = CounterEvent.increment
        val change = Change(0,1)
        val transition = Transition(1,CounterEvent.increment,1)

        DefaultBlocObserver().onError(bloc  as Bloc<Any,Any>,error)
    }

    @Test
    fun OnClose_does_nothing_by_default() {
        val bloc=CounterBloc(GlobalScope)
        val error=Exception()
        val event = CounterEvent.increment
        val change = Change(0,1)
        val transition = Transition(1,CounterEvent.increment,1)

        DefaultBlocObserver().onClose(bloc  as Bloc<Any,Any>)
    }

}
