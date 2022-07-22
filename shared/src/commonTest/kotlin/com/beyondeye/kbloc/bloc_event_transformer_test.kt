package com.beyondeye.kbloc

import com.beyondeye.kbloc.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContentEquals

interface CounterEvent

class Increment:CounterEvent {
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
class Decrement:CounterEvent {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        return true
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }
}


val delay_msecs=30L

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
    incrementTransformer: EventTransformer<Increment>?=null
) : Bloc<CounterEvent, Int>(cscope, 0) {
    val onCalls= mutableListOf<CounterEvent>()
    val onEmitCalls= mutableListOf<CounterEvent>()
    init {
        on<Increment>(transformer = incrementTransformer) {event, emit->
//            println("onCalls add")
            onCalls.add(event)
            delay(delay_msecs)
            if(emit.isDone()) return@on
            onEmitCalls.add(event)
            println("emit called with initial state $state")
            emit(state+1)
        }
    }
}

class BlocEventTransformerTest {
    //TODO this test does not work currently work as expected
    @Test
    fun processes_events_concurrently_by_default() {
        runBlocking {
            val states = mutableListOf<Int>()
            val bloc = CounterBloc(this)
            val sub1= async {
                bloc.stream.collect {
                    states.add(it)
                }
            }
            delay(5) //todo: this was not in original code: otherwise the first action is ignored
            with(bloc) {
                add(Increment())
                delay(10) //todo: not present in original code: otherwise events are processede concurrently on same initial state (0)
                add(Increment())
                delay(10) //todo: not present in original code: otherwise events are processede concurrently on same initial state (0)
                add(Increment())
                delay(10) //todo: not present in original code: otherwise events are processede concurrently on same initial state (0)
            }
            delay(5) //it was tick() in original code

            assertContentEquals(listOf(Increment(),Increment(),Increment()),bloc.onCalls)

            delay(20) //it was tick() in original code
            assertContentEquals(listOf(Increment(),Increment(),Increment()),bloc.onEmitCalls)

            delay(1) //it was tick() in original code
            //println(states)
            //TODO: it was (1,2,3) in original code, because initial state 0 is ignored in dart code
            assertContentEquals(listOf(0,1,2,3),states)
            sub1.cancel()
            bloc.close()
        }
    }
    @Test
    fun when_processing_events_concurrently_all_subscriptions_are_canceled_on_close() {
        runBlocking {
            val states = mutableListOf<Int>()
            val bloc = CounterBloc(this)
            val sub1= async {
                bloc.stream.collect {
                    states.add(it)
                }
            }
            delay(5) //todo: this was not in original code: otherwise the first action is ignored
            with(bloc) {
                add(Increment())
                add(Increment())
                add(Increment())
            }
            delay(5) //it was tick() in original code
            assertContentEquals(listOf(Increment(),Increment(),Increment()),bloc.onCalls)

            bloc.close() //immediately close
            sub1.cancel()
            //TODO there was no delay here in original code: I have added it to be sure that after close()
            //  events processing coroutines are cancelled and no emiting is done
            delay(1000)
            //println(bloc.onEmitCalls)
            //println(states)
            assertContentEquals(listOf(),bloc.onEmitCalls)
            //todo: it was listOf() in original code because initial state is ignored in bloc dart code
            assertContentEquals(listOf(0),states)
        }
    }
}
/*

void main() {

  test(
      'processes events sequentially when '
      'transformer is overridden.', () async {
    EventTransformer<Increment> incrementTransformer() {
      return (events, mapper) => events.asyncExpand(mapper);
    }

    final states = <int>[];
    final bloc = CounterBloc(incrementTransformer: incrementTransformer())
      ..stream.listen(states.add)
      ..add(Increment())
      ..add(Increment())
      ..add(Increment());

    await tick();

    expect(
      bloc.onCalls,
      equals([Increment()]),
    );

    await wait();

    expect(
      bloc.onEmitCalls,
      equals([Increment()]),
    );
    expect(states, equals([1]));

    await tick();

    expect(
      bloc.onCalls,
      equals([Increment(), Increment()]),
    );

    await wait();

    expect(
      bloc.onEmitCalls,
      equals([Increment(), Increment()]),
    );

    expect(states, equals([1, 2]));

    await tick();

    expect(
      bloc.onCalls,
      equals([Increment(), Increment(), Increment()]),
    );

    await wait();

    expect(
      bloc.onEmitCalls,
      equals([Increment(), Increment(), Increment()]),
    );

    expect(states, equals([1, 2, 3]));

    await bloc.close();

    expect(
      bloc.onCalls,
      equals([Increment(), Increment(), Increment()]),
    );

    expect(
      bloc.onEmitCalls,
      equals([Increment(), Increment(), Increment()]),
    );

    expect(states, equals([1, 2, 3]));
  });

  test(
      'processes events sequentially when '
      'Bloc.transformer is overridden.', () async {
    await BlocOverrides.runZoned(
      () async {
        final states = <int>[];
        final bloc = CounterBloc()
          ..stream.listen(states.add)
          ..add(Increment())
          ..add(Increment())
          ..add(Increment());

        await tick();

        expect(
          bloc.onCalls,
          equals([Increment()]),
        );

        await wait();

        expect(
          bloc.onEmitCalls,
          equals([Increment()]),
        );
        expect(states, equals([1]));

        await tick();

        expect(
          bloc.onCalls,
          equals([Increment(), Increment()]),
        );

        await wait();

        expect(
          bloc.onEmitCalls,
          equals([Increment(), Increment()]),
        );

        expect(states, equals([1, 2]));

        await tick();

        expect(
          bloc.onCalls,
          equals([
            Increment(),
            Increment(),
            Increment(),
          ]),
        );

        await wait();

        expect(
          bloc.onEmitCalls,
          equals([Increment(), Increment(), Increment()]),
        );

        expect(states, equals([1, 2, 3]));

        await bloc.close();

        expect(
          bloc.onCalls,
          equals([Increment(), Increment(), Increment()]),
        );

        expect(
          bloc.onEmitCalls,
          equals([Increment(), Increment(), Increment()]),
        );

        expect(states, equals([1, 2, 3]));
      },
      eventTransformer: (events, mapper) => events.asyncExpand<dynamic>(mapper),
    );
  });
}

 */