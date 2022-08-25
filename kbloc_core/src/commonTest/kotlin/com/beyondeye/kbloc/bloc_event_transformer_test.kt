@file:OptIn(ExperimentalCoroutinesApi::class)

package com.beyondeye.kbloc

import com.beyondeye.kbloc.concurrency.EventTransformer_sequential
import com.beyondeye.kbloc.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

interface CounterEvent

class Increment : CounterEvent {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        return true
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }
}

//DARIO
class Decrement : CounterEvent {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        return true
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }
}


val delay_msecs = 30L

suspend fun wait() {
    delay(delay_msecs)
}

/* this method already defined elsewhere
suspend fun tick() {
    delay(0)
}
 */

class CounterBloc(
    cscope: CoroutineScope,
    incrementTransformer: EventTransformer<Increment>? = null
) : Bloc<CounterEvent, Int>(cscope, 0, false, false) {
    val onCalls = mutableListOf<CounterEvent>()
    val onEmitCalls = mutableListOf<CounterEvent>()

    init {
        on<Increment>(transformer = incrementTransformer) { event, emit ->
//            println("onCalls add")
            onCalls.add(event)
            delay(delay_msecs)
            if (emit.isDone()) return@on
            onEmitCalls.add(event)
            println("emit called with initial state $state")
            emit(state + 1)
        }
    }
}

class BlocEventTransformerTest {
    //TODO this test does not work currently work as expected
    // see https://developer.android.com/kotlin/coroutines/test#invoking-suspending-functions
    @Test
    fun processes_events_concurrently_by_default() = runTest {
        val states = mutableListOf<Int>()
        val bloc = CounterBloc(this)
        val sub1 = async {
            bloc.stream.collect {
                states.add(it)
            }
        }
        delay(5) //todo: this was not in original code: otherwise the first action is ignored
        with(bloc) {
            add_sync(Increment())
            delay(10) //todo: not present in original code: otherwise events are processede concurrently on same initial state (0)
            add_sync(Increment())
            delay(10) //todo: not present in original code: otherwise events are processede concurrently on same initial state (0)
            add_sync(Increment())
            delay(10) //todo: not present in original code: otherwise events are processede concurrently on same initial state (0)
        }
        delay(5) //it was tick() in original code

        assertContentEquals(listOf(Increment(), Increment(), Increment()), bloc.onCalls)

        delay(20) //it was tick() in original code
        assertContentEquals(listOf(Increment(), Increment(), Increment()), bloc.onEmitCalls)

        delay(1) //it was tick() in original code
        //println(states)
        //TODO: it was (1,2,3) in original code, because initial state 0 is ignored in dart code
        assertContentEquals(listOf(0, 1, 2, 3), states)
        sub1.cancel()
        bloc.close()
    }

    @Test
    fun when_processing_events_concurrently_all_subscriptions_are_canceled_on_close() =
        runTest {
            val states = mutableListOf<Int>()
            val bloc = CounterBloc(this)
            val sub1 = async {
                bloc.stream.collect {
                    states.add(it)
                }
            }
            delay(5) //todo: this was not in original code: otherwise the first action is ignored
            with(bloc) {
                add_sync(Increment())
                add_sync(Increment())
                add_sync(Increment())
            }
            delay(5) //it was tick() in original code
            assertContentEquals(listOf(Increment(), Increment(), Increment()), bloc.onCalls)

            bloc.close() //immediately close
            sub1.cancel()
            //TODO there was no delay here in original code: I have added it to be sure that after close()
            //  events processing coroutines are cancelled and no emiting is done
            delay(1000)
            //println(bloc.onEmitCalls)
            //println(states)
            assertContentEquals(listOf(), bloc.onEmitCalls)
            //todo: it was listOf() in original code because initial state is ignored in bloc dart code
            assertContentEquals(listOf(0), states)
        }

    @Test
    fun processes_events_sequentially_when_transformer_is_overridden() =
        runTest {
            val incrementTransformer: EventTransformer<Increment> =
                EventTransformer_sequential() // { events, mapper -> events.asyncExpand(mapper)  }
            val states = mutableListOf<Int>()
            val bloc = CounterBloc(this, incrementTransformer)
            val sub1 = async {
                bloc.stream.collect {
                    states.add(it)
                }
            }
            with(bloc) {
                add_sync(Increment())
                add_sync(Increment())
                add_sync(Increment())
            }
            //---------
            delay(10) //todo it was tick() in original code
            assertContentEquals(listOf(Increment()), bloc.onCalls)
            wait()
            assertContentEquals(listOf(Increment()), bloc.onEmitCalls)
            println(states)
            assertContentEquals(listOf(0, 1), states)       //listOf(1) in original code
            //---------
            delay(10) //todo it was tick() in original code
            assertContentEquals(listOf(Increment(), Increment()), bloc.onCalls)
            wait()
            assertContentEquals(listOf(Increment(), Increment()), bloc.onEmitCalls)
            assertContentEquals(listOf(0, 1, 2), states)       //listOf(1,2) in original code
            //---------
            delay(10) //todo it was tick() in original code
            assertContentEquals(listOf(Increment(), Increment(), Increment()), bloc.onCalls)
            wait()
            assertContentEquals(listOf(Increment(), Increment(), Increment()), bloc.onEmitCalls)
            assertContentEquals(listOf(0, 1, 2, 3), states)       //listOf(1,2,3) in original code

            bloc.close() //immediately close
            sub1.cancel()
        }

    @Test
    fun processes_events_sequentially_when_bloc_transformer_is_overridden() = runTest {

        BlocOverrides.setGlobalOverrides(
            eventTransformer = EventTransformer_sequential()
        ) //{ events, mapper -> events.asyncExpand<Any, Any>(mapper) }
        val incrementTransformer: EventTransformer<Increment> =
            EventTransformer_sequential() // { events, mapper -> events.asyncExpand(mapper) }
        val states = mutableListOf<Int>()
        val bloc = CounterBloc(this, incrementTransformer)
        val sub1 = async {
            bloc.stream.collect {
                states.add(it)
            }
        }
        with(bloc) {
            add_sync(Increment())
            add_sync(Increment())
            add_sync(Increment())
        }
        //---------
        delay(10) //todo it was tick() in original code
        assertContentEquals(listOf(Increment()), bloc.onCalls)
        wait()
        assertContentEquals(listOf(Increment()), bloc.onEmitCalls)
        println(states)
        assertContentEquals(listOf(0, 1), states)       //listOf(1) in original code
        //---------
        delay(10) //todo it was tick() in original code
        assertContentEquals(listOf(Increment(), Increment()), bloc.onCalls)
        wait()
        assertContentEquals(listOf(Increment(), Increment()), bloc.onEmitCalls)
        assertContentEquals(listOf(0, 1, 2), states)       //listOf(1,2) in original code
        //---------
        delay(10) //todo it was tick() in original code
        assertContentEquals(listOf(Increment(), Increment(), Increment()), bloc.onCalls)
        wait()
        assertContentEquals(listOf(Increment(), Increment(), Increment()), bloc.onEmitCalls)
        assertContentEquals(
            listOf(0, 1, 2, 3),
            states
        )       //listOf(1,2,3) in original code

        bloc.close() //immediately close
        sub1.cancel()
    }
}
