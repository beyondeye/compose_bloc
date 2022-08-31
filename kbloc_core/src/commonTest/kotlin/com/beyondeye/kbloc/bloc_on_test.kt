@file:OptIn(ExperimentalCoroutinesApi::class)

package com.beyondeye.kbloc

import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.Emitter
import com.beyondeye.kbloc.core.StateError
import io.mockk.MockKAnnotations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


interface TestEvent


open class TestEventA : TestEvent {}

open class TestEventAA : TestEventA() {}

open class TestEventB : TestEvent {}

class TestEventBA : TestEventB() {}

class TestState

typealias onEvent<E,S> = (event:E, emit: Emitter<S>) -> Unit

fun <E,S> defaultOnEvent(event:Any, emit:Emitter<Any>)  {   }

class TestBloc(cscope:CoroutineScope,
               val onTestEvent :onEvent<TestEvent,TestState> = { _,_ -> },
               val onTestEventA :onEvent<TestEventA,TestState> = { _,_ -> },
               val onTestEventB :onEvent<TestEventB,TestState> = { _,_ -> },
               val onTestEventAA :onEvent<TestEventAA,TestState> = { _,_ -> },
               val onTestEventBA :onEvent<TestEventBA,TestState> = { _,_ -> },

) : Bloc<TestEvent, TestState>(cscope, TestState(),false,false)
{
    init {
        on<TestEventA>(handler = onTestEventA)
        on<TestEventB>(handler = onTestEventB)
        on<TestEventAA>(handler = onTestEventAA)
        on<TestEventBA>(handler = onTestEventBA)
        on<TestEvent>(handler = onTestEvent)
    }
}
class DuplicateHandlerBloc(cscope: CoroutineScope) : Bloc<TestEvent,TestState>(cscope, TestState(),
    false,false) {
    init {
        on<TestEvent> {_,_-> }
        on<TestEvent> {_,_-> }
    }
}

class MissingHandlerBloc(cscope: CoroutineScope) : Bloc<TestEvent,TestState>(cscope, TestState(),
    false,false) {
    init {

    }
}
class BlocOnTest {
    @BeforeTest
    //see https://sonique6784.medium.com/pure-kotlin-unit-testing-mocking-part-2-e13857014ea6
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun onEvent_throws_StateError_when_handler_is_registered_more_than_once()
    {
        assertFailsWith<Exception>(Bloc.getEventMultipleRegistrationErrorMessage(TestEvent::class)) { DuplicateHandlerBloc(GlobalScope)  }
    }

    @Test
    fun onEvent_throws_StateError_when_handler_is_missing()
    {
        assertFailsWith<StateError>(Bloc.getHandlerMissingErrorMessage(TestEventA::class))
        {
            MissingHandlerBloc(GlobalScope).add(TestEventA())
        }
    }

    @Test
    fun onEvent_invokes_all_on_T_when_event_E_is_added_where_E_is_T() =
        runTest(UnconfinedTestDispatcher()) {
            var onEventCallCount = 0
            var onACallCount = 0
            var onBCallCount = 0
            var onAACallCount = 0
            var onBACallCount = 0

            val bloc = TestBloc(
                    onTestEvent= {_, _ -> onEventCallCount++ },
            onTestEventA= {_, _ -> onACallCount++},
            onTestEventB= { _, _ -> onBCallCount++},
            onTestEventAA= {_, _ -> onAACallCount++},
            onTestEventBA= {_, _ -> onBACallCount++},
                cscope = this
            )
            bloc.add_sync(TestEventA())
            delay(0)

            assertEquals(1,onEventCallCount)
            assertEquals(1,onACallCount)
            assertEquals(0,onBCallCount)
            assertEquals(0,onAACallCount)
            assertEquals(0,onBACallCount)

            bloc.add_sync(TestEventAA())

            delay(0)

            assertEquals(2,onEventCallCount)
            assertEquals(2,onACallCount)
            assertEquals(0,onBCallCount)
            assertEquals(1,onAACallCount)
            assertEquals(0,onBACallCount)

            bloc.add_sync(TestEventB())

            delay(0)

            assertEquals(3,onEventCallCount)
            assertEquals(2,onACallCount)
            assertEquals(1,onBCallCount)
            assertEquals(1,onAACallCount)
            assertEquals(0,onBACallCount)

            bloc.add_sync(TestEventBA())

            delay(0)

            assertEquals(4,onEventCallCount)
            assertEquals(2,onACallCount)
            assertEquals(2,onBCallCount)
            assertEquals(1,onAACallCount)
            assertEquals(1,onBACallCount)


            bloc.close()

        }

}