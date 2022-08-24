@file:OptIn(ExperimentalCoroutinesApi::class)

package com.beyondeye.kbloc

import com.beyondeye.kbloc.core.*
import com.beyondeye.kbloc.cubits.CounterCubit
import com.beyondeye.kbloc.cubits.FakeAsyncCounterCubit
import com.beyondeye.kbloc.cubits.SeededCubit
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class MockBlocObserver<T : Any> : BlocObserver<T> {}
//class FakeBlocBase<T:Any>: BlocBase<T>(Any) {}
//class FakeChange<S> : Change<S>() {}


class CubitTests {
    @BeforeTest
    //see https://sonique6784.medium.com/pure-kotlin-unit-testing-mocking-part-2-e13857014ea6
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun constructor_triggers_onCreate_on_observer() {
        val observer = spyk<MockBlocObserver<Any>>()
        BlocOverrides.runWithOverrides(blocObserver = observer) {
            val cubit = CounterCubit()
            //see https://mockk.io/#verification-atleast-atmost-or-exactly-times
            verify(exactly = 1) { observer.onCreate(cubit as BlocBase<Any>) }
        }
    }

    @Test
    fun initial_state_is_correct() {
        val cubit = CounterCubit()
        assertEquals(0, cubit.state)
    }

    @Test
    fun addError_triggers_onError() {
        val observer = spyk<MockBlocObserver<Any>>()
        val expectedError = Exception("fatal exception")
        val errors = mutableListOf<Throwable>()
        BlocOverrides.runWithOverrides(blocObserver = observer) {
            val cubit = CounterCubit(onErrorCallback = { e -> errors.add(e) })
            cubit.addError(expectedError)

            assertEquals(1, errors.size)
            assertEquals(expectedError, errors[0])
            verify(exactly = 1) { observer.onError(cubit as BlocBase<Any>, expectedError) }
        }
    }

    @Test
    fun onChange_is_not_called_for_the_initial_state() = runTest {
        val observer = spyk<MockBlocObserver<Any>>()
        BlocOverrides.setGlobalOverrides(blocObserver = observer)
        val changes = mutableListOf<Change<Any>>()
        val cubit = CounterCubit(onChangeCallback = { changes.add(it as Change<Any>) })
        cubit.close()
        assertEquals(0, changes.size)
        verify(exactly = 0) { observer.onChange(allAny(), allAny()) }
    }

    @Test
    fun onChange_is_called_with_correct_change_for_a_single_state_change() = runTest {
        val observer = spyk<MockBlocObserver<Any>>()
        BlocOverrides.setGlobalOverrides(blocObserver = observer)
        val changes = mutableListOf<Change<Any>>()
        val cubit = CounterCubit(onChangeCallback = { changes.add(it as Change<Any>) })
        cubit.increment()
        cubit.close()
        assertEquals(1, changes.size)
        verify(exactly = 1) { observer.onChange(cubit as BlocBase<Any>, Change(0, 1)) }
    }

    @Test
    fun onChange_is_called_with_correct_changes_for_multiple_state_changes() = runTest {
        val observer = spyk<MockBlocObserver<Any>>()
        BlocOverrides.setGlobalOverrides(blocObserver = observer)
        val changes = mutableListOf<Change<Int>>()
        val cubit = CounterCubit(onChangeCallback = { changes.add(it) })
        cubit.increment()
        cubit.increment()
        cubit.close()
        assertContentEquals(
            mutableListOf(Change(0, 1), Change(1, 2)),
            changes
        )
        verify(exactly = 1) { observer.onChange(cubit as BlocBase<Any>, Change(0, 1)) }
        verify(exactly = 1) { observer.onChange(cubit as BlocBase<Any>, Change(1, 2)) }
    }

    //note runTest for testing suspending code see: https://developer.android.com/kotlin/coroutines/test#invoking-suspending-functions
//see also https://developer.android.com/kotlin/coroutines/test#unconfinedtestdispatcher
    @Test
    fun emit_throws_StateError_if_cubit_is_closed() = runTest(UnconfinedTestDispatcher()) {
        var didThrow = false
        val cubit = CounterCubit()
        val collected_emission = mutableListOf<Int>()
        //with UnconfinedTestDispatcher coroutines are run eagerly so that we can immediately obtain
        //values emitted in flow see https://developer.android.com/kotlin/flow/test#continuous-collection
        val collectJob = launch(UnconfinedTestDispatcher()) {
            cubit.stream.collect {
                collected_emission.add(it)
            }
        }
        try {
            with(cubit) {
                increment()
                close()
                increment()
            }
        } catch (e: Throwable) {
            didThrow = true
            assertIs<StateError>(e)
        }
        assertTrue { didThrow }
        //todo in the original test code, expected emission is listOf(1) but probably this is because we test it differently TODO: check if I should change something in code implementation
        assertContentEquals(listOf(0, 1), collected_emission)
        collectJob.cancel()
    }

    @Test
    fun emit_states_in_the_correct_order() = runTest(UnconfinedTestDispatcher()) {
        val states = mutableListOf<Int>()
        val cubit = CounterCubit()
        val subscription = launch {
            cubit.stream.collect {
                states.add(it)
            }
        }
        cubit.increment()
        cubit.close()
        subscription.cancel()
        println(states)
        //note that in the original test the expected states where listOf(1) TODO: check if I should change something in code implementation
        assertContentEquals(listOf(0, 1), states)
    }

    @Test
    fun can_emit_initial_state_only_once() = runTest(UnconfinedTestDispatcher()) {
        val states = mutableListOf<Int>()
        val cubit = SeededCubit(0)
        val subscription = launch {
            cubit.stream.collect {
                states.add(it)
            }
        }
        with(cubit) {
            emitState(0)
            emitState(0)
        }
        cubit.close()
        subscription.cancel()
        println(states)
        assertContentEquals(listOf(0), states)
    }

    @Test
    fun can_emit_initial_state_and_continue_emitting_distinct_states() =
        runTest(UnconfinedTestDispatcher()) {
            val states = mutableListOf<Int>()
            val cubit = SeededCubit(0)
            val subscription = launch {
                cubit.stream.collect {
                    states.add(it)
                }
            }
            with(cubit) {
                emitState(0)
                emitState(1)
            }
            cubit.close()
            subscription.cancel()
            println(states)
            assertContentEquals(listOf(0, 1), states)
        }

    @Test
    fun does_not_emit_duplicate_states() = runTest(UnconfinedTestDispatcher()) {
        val states = mutableListOf<Int>()
        val cubit = SeededCubit(0)
        val subscription = launch {
            cubit.stream.collect {
                states.add(it)
            }
        }
        with(cubit) {
            emitState(1)
            emitState(1)
            emitState(2)
            emitState(2)
            emitState(3)
            emitState(3)
        }
        cubit.close()
        subscription.cancel()
        println(states)
        //note that in the original test the expected states where listOf(1,2,3) TODO: check if I should change something in code implementation
        assertContentEquals(listOf(0, 1, 2, 3), states)
    }

    //TODO this test currently fails because the kotlin implementation currently always gives a Cubit a non null initial state
    @Test
    fun does_not_receive_current_state_upon_subscribing() = runTest(UnconfinedTestDispatcher()) {
        val states = mutableListOf<Int>()
        val cubit = CounterCubit()
        val subscription = launch {
            cubit.stream.collect {
                states.add(it)
            }
        }
        cubit.close()
        subscription.cancel()
        println(states)
        //note that in the original test the expected states where listOf(1,2,3) TODO: check if I should change something in code implementation
        assertTrue { states.isEmpty() }
    }

    @Test
    fun receive_single_async_state() = runTest(UnconfinedTestDispatcher()) {
        val states = mutableListOf<Int>()
        val cubit = FakeAsyncCounterCubit(0)
        val subscription = launch {
            cubit.stream.collect {
                states.add(it)
            }
        }
        cubit.increment()
        cubit.close()
        subscription.cancel()
        println(states)
        //TODO: in original bloc code, FakeAsyncCounterCubit has no initial state so that the list of received state is listOf(1)
        assertContentEquals(listOf(0, 1), states)
    }

    @Test
    fun receive_multiple_async_states() = runTest(UnconfinedTestDispatcher()) {
        val states = mutableListOf<Int>()
        val cubit = FakeAsyncCounterCubit(0)
        val subscription = launch {
            cubit.stream.collect {
                states.add(it)
            }
        }
        cubit.increment()
        cubit.increment()
        cubit.increment()
        cubit.close()
        subscription.cancel()
        println(states)
        //TODO: in original bloc code, FakeAsyncCounterCubit has no initial state so that the list of received state is listOf(1,2,3)
        assertContentEquals(listOf(0, 1, 2, 3), states)
    }

    @Test
    fun can_call_listen_multiple_times() = runTest(UnconfinedTestDispatcher()) {
        val states = mutableListOf<Int>()
        val cubit = CounterCubit()
        val subscription1 = launch {
            cubit.stream.collect {
                states.add(it)
            }
        }
        val subscription2 = launch {
            cubit.stream.collect {
                states.add(it)
            }
        }
        cubit.increment()

        cubit.close()
        subscription1.cancel()
        subscription2.cancel()
        println(states)
        //TODO: in original bloc code, CounterCubit has no initial state so that the list of received state is listOf(1,1)
        assertContentEquals(listOf(0, 0, 1, 1), states)
    }

    @Test
    fun triggers_onClose_on_observer() = runTest {
        val observer = spyk<MockBlocObserver<Any>>()
        BlocOverrides.setGlobalOverrides(blocObserver = observer)
        val cubit = CounterCubit()
        cubit.close()
        verify(exactly = 1) { observer.onClose(cubit as BlocBase<Any>) }
    }

    @Test
    fun emits_done_sync() = runTest {
        val cubit = CounterCubit()
        cubit.close()
        //TODO in the original code it is possible to receive the complete() stream status. it is not
        // currently possible in kotlin because SharedFlow never completes: perhaps reimplement it wrapping it in a completable flow
        //original code:        expect(cubit.stream, emitsDone);
        assertTrue { cubit.isClosed }
    }

    @Test
    fun emits_done_async() = runTest(UnconfinedTestDispatcher()) {
        val cubit = CounterCubit()
        cubit.close()
        //TODO in the original code it is possible to receive the complete() stream status. it is not
        // currently possible in kotlin because SharedFlow never completes: perhaps reimplement it wrapping it in a completable flow
        //original code:        expect(cubit.stream, emitsDone);
        assertTrue { cubit.isClosed }
    }

    @Test
    fun returns_true_after_cubit_is_closed() = runTest(UnconfinedTestDispatcher()) {
        val cubit = CounterCubit()
        assertFalse { cubit.isClosed }
        cubit.close()
        assertTrue { cubit.isClosed }
    }
}
