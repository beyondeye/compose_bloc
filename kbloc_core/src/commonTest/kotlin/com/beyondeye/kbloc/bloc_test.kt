@file:OptIn(ExperimentalCoroutinesApi::class)

package com.beyondeye.kbloc

import com.beyondeye.kbloc.async.AsyncBloc
import com.beyondeye.kbloc.async.AsyncState
import com.beyondeye.kbloc.complex.*
import com.beyondeye.kbloc.core.*
import com.beyondeye.kbloc.counter.CounterBloc
import com.beyondeye.kbloc.counter.CounterEvent
import com.beyondeye.kbloc.counter.MergeBloc
import com.beyondeye.kbloc.seeded.SeededBloc
import com.beyondeye.kbloc.simple.SimpleBloc
import com.beyondeye.kbloc.stream.*
import io.mockk.MockKAnnotations
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.*

internal suspend fun tick() {
    delay(0)
}

//already defined in cubit_test
//class MockBlocObserver<T:Any> : BlocObserver<T> {}

//class FakeBlocBase<S> extends Fake implements BlocBase<S> {}

class SimpleBlocTests {
    /*
    lateinit var  simpleBloc: SimpleBloc
    lateinit var observer:MockBlocObserver<Any>
    fun setup_simple_bloc(cscope:CoroutineScope) {
        simpleBloc = SimpleBloc(cscope)
        observer = spyk(MockBlocObserver())
    }
     */
    @BeforeTest
    //see https://sonique6784.medium.com/pure-kotlin-unit-testing-mocking-part-2-e13857014ea6
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

    }

    @Test
    fun simple_bloc_triggers_onCreate_on_observer_when_instantiated() {
        val observer = spyk(MockBlocObserver<Any>())
        BlocOverrides.runWithOverrides(blocObserver = observer) {
            val bloc = SimpleBloc(GlobalScope)
            verify(exactly = 1) { observer.onCreate(bloc as BlocBase<Any>) }
        }
    }

    @Test
    fun simple_bloc_triggers_onClose_on_observer_when_closed() {
        val observer = spyk(MockBlocObserver<Any>())
        BlocOverrides.runWithOverrides(blocObserver = observer) {
            runTest(UnconfinedTestDispatcher()) {
                val bloc = SimpleBloc(this)
                bloc.close()
                verify(exactly = 1) { observer.onClose(bloc as BlocBase<Any>) }
            }
        }
    }
    /*
      test('close does not emit new states over the state stream', () async {
        final expectedStates = [emitsDone];

        unawaited(expectLater(simpleBloc.stream, emitsInOrder(expectedStates)));

        await simpleBloc.close();
      });
     */

    @Test
    fun simple_bloc_state_returns_correct_value_initially() {
        val bloc = SimpleBloc(GlobalScope)
        assertEquals("", bloc.state)
    }

    @Test
    fun simple_bloc_should_map_single_event_to_correct_state() {
        val observer = spyk(MockBlocObserver<Any>())
        BlocOverrides.runWithOverrides(blocObserver = observer) {
            runTest(UnconfinedTestDispatcher()) {
                val bloc = SimpleBloc(this)
                bloc.add_sync("event")
                bloc.close()

                verify(exactly = 1) {
                    observer.onTransition(
                        bloc as Bloc<Any, Any>,
                        Transition("", "event", "data")
                    )
                }
                verify(exactly = 1) {
                    observer.onChange(bloc as Bloc<Any, Any>, Change("", "data"))
                }
                assertEquals("data", bloc.state)
            }
        }
    }

    @Test
    fun simple_bloc_should_map_multiple_events_to_correct_states() {
        val observer = spyk(MockBlocObserver<Any>())
        BlocOverrides.runWithOverrides(blocObserver = observer) {
            runTest(UnconfinedTestDispatcher()) {
                val bloc = SimpleBloc(this)
                with(bloc) {
                    add_sync("event1")
                    add_sync("event2")
                    add_sync("event3")
                    close()
                }

                verify(exactly = 1) {
                    observer.onTransition(
                        bloc as Bloc<Any, Any>,
                        Transition("", "event1", "data")
                    )
                }
                verify(exactly = 1) {
                    observer.onChange(bloc as Bloc<Any, Any>, Change("", "data"))
                }
                assertEquals("data", bloc.state)
            }
        }
    }

    @Test
    fun simple_bloc_is_a_broadcast_stream() {
        val observer = spyk(MockBlocObserver<Any>())
        BlocOverrides.runWithOverrides(blocObserver = observer) {
            runTest(UnconfinedTestDispatcher()) {
                val bloc = SimpleBloc(this)
                val sub1data = mutableListOf<String>()
                val sub2data = mutableListOf<String>()
                val expected_states = listOf("", "data")
                val sub1 = async {
                    bloc.stream.collect {
                        sub1data.add(it)
                    }
                }
                val sub2 = async {
                    bloc.stream.collect {
                        sub2data.add(it)
                    }
                }

                with(bloc) {
                    add_sync("event")
                    close()
                }
                sub1.cancel()
                sub2.cancel()

                assertContentEquals(expected_states, sub1data)
                assertContentEquals(expected_states, sub2data)
            }
        }
    }

    /*
            //TODO what is different in this test from the previous one?
          test('multiple subscribers receive the latest state', () {
            final expectedStates = const <String>['data'];

            expectLater(simpleBloc.stream, emitsInOrder(expectedStates));
            expectLater(simpleBloc.stream, emitsInOrder(expectedStates));
            expectLater(simpleBloc.stream, emitsInOrder(expectedStates));

            simpleBloc.add('event');
          });
        });
     */
    @Test
    fun Complex_Bloc_close_does_no_emit_new_states_over_the_state_stream() = runTest(
        UnconfinedTestDispatcher()
    )
    {
        val observer = spyk(MockBlocObserver<Any>())
        val expectedStates = listOf<ComplexState>(ComplexStateA())
        val actualStates = mutableListOf<ComplexState>()
        val complexBloc = ComplexBloc(this)
        val sub1 = async {
            complexBloc.stream.collect {
                actualStates.add(it)
            }
        }
        //TODO why I need such a lenghty interval to receive the initial complexBloc state of complexBloc, from the stream subscription?
        delay(50)
        complexBloc.close()
        sub1.cancel()
        assertContentEquals(expectedStates, actualStates)
    }

    @Test
    fun Complex_Bloc_state_returns_correct_value_initially() = runTest(
        UnconfinedTestDispatcher()
    )
    {
        val complexBloc = ComplexBloc(this)
        assertEquals(ComplexStateA(), complexBloc.state)
        complexBloc.close()
    }

    @Test
    fun ComplexBloc_should_map_single_event_to_correct_state() {
        val observer = spyk(MockBlocObserver<Any>())
        BlocOverrides.runWithOverrides(observer) {
            runTest(UnconfinedTestDispatcher())
            {
                val expectedStates = listOf(ComplexStateB())
                val actualStates = mutableListOf<ComplexState>()
                val complexBloc = ComplexBloc(this)
                val sub1 = async {
                    complexBloc.stream.collect {
                        actualStates.add(it)
                    }
                }
                complexBloc.add_sync(ComplexEventB())
                //TODO why I need such a lenghty interval to receive the initial complexBloc state of complexBloc, from the stream subscription?
                delay(50)
                assertContentEquals(expectedStates, actualStates)

                verify(exactly = 1) {
                    observer.onTransition(
                        complexBloc as Bloc<Any, Any>,
                        Transition(ComplexStateA(), ComplexEventB(), ComplexStateB())
                    )
                }
                verify(exactly = 1) {
                    observer.onChange(
                        complexBloc as BlocBase<Any>,
                        Change(ComplexStateA(), ComplexStateB())
                    )
                }

                complexBloc.close()
                sub1.cancel()
            }
        }
    }

    @Test
    fun ComplexBloc_should_map_multiple_events_to_correct_state() {
        val observer = spyk(MockBlocObserver<Any>())
        BlocOverrides.runWithOverrides(observer) {
            runTest(UnconfinedTestDispatcher())
            {
                val expectedStates = listOf(
                    ComplexStateB(),
                    ComplexStateD(),
                    ComplexStateA(),
                    ComplexStateC()
                )

                val actualStates = mutableListOf<ComplexState>()
                val complexBloc = ComplexBloc(this)
                val sub1 = async {
                    complexBloc.stream.collect {
                        actualStates.add(it)
                    }
                }
                complexBloc.add_sync(ComplexEventA())
                delay(20)
                complexBloc.add_sync(ComplexEventB())
                delay(20)
                complexBloc.add_sync(ComplexEventC())
                delay(20)
                complexBloc.add_sync(ComplexEventD())
                delay(200)
                with(complexBloc) {
                    add_sync(ComplexEventC())
                    add_sync(ComplexEventA())
                }
                delay(120)
                complexBloc.add_sync(ComplexEventC())
                //TODO why I need such a lenghty interval to receive the added state from the stream subscription?
                delay(30)
                assertContentEquals(expectedStates, actualStates)

                sub1.cancel()
                complexBloc.close()
            }
        }
    }

    @Test
    fun ComplexBloc_is_a_broadcast_stream() = runTest(UnconfinedTestDispatcher())
    {
        val expectedStates = listOf(
            ComplexStateB(),
        )

        val complexBloc = ComplexBloc(this)
        val actualStates1 = mutableListOf<ComplexState>()
        val sub1 = async {
            complexBloc.stream.collect {
                actualStates1.add(it)
            }
        }
        val actualStates2 = mutableListOf<ComplexState>()
        val sub2 = async {
            complexBloc.stream.collect {
                actualStates2.add(it)
            }
        }

        complexBloc.add_sync(ComplexEventB())
        //TODO why I need such a lenghty interval to receive the added state from the stream subscription?
        delay(50)
        assertContentEquals(expectedStates, actualStates1)
        assertContentEquals(expectedStates, actualStates2)
        sub1.cancel()
        sub2.cancel()
        complexBloc.close()
    }

    @Test
    fun ComplexBloc_multiple_subscribers_receive_the_latest_state() =
        runTest(UnconfinedTestDispatcher())
        {
            val expectedStates = listOf(
                ComplexStateB(),
            )

            val complexBloc = ComplexBloc(this)
            val actualStates1 = mutableListOf<ComplexState>()
            val sub1 = async {
                complexBloc.stream.collect {
                    actualStates1.add(it)
                }
            }
            val actualStates2 = mutableListOf<ComplexState>()
            val sub2 = async {
                complexBloc.stream.collect {
                    actualStates2.add(it)
                }
            }
            val actualStates3 = mutableListOf<ComplexState>()
            val sub3 = async {
                complexBloc.stream.collect {
                    actualStates3.add(it)
                }
            }

            complexBloc.add_sync(ComplexEventB())
            //TODO why I need such a lenghty interval to receive the added state from the stream subscription?
            delay(50)
            assertContentEquals(expectedStates, actualStates1)
            assertContentEquals(expectedStates, actualStates2)
            assertContentEquals(expectedStates, actualStates3)
            sub1.cancel()
            sub2.cancel()
            sub3.cancel()
            complexBloc.close()
        }

    @Test
    fun CounterBloc_state_returns_correct_value_initially() = runTest(UnconfinedTestDispatcher()) {
        val events = mutableListOf<CounterEvent>()
        val transitions = mutableListOf<String>()
        val counterBloc = CounterBloc(this,
            onEventCallback = { events.add(it) },
            onTransitionCallback = { transition ->
                transitions.add(transition.toString())
            })
        assertEquals(0, counterBloc.state)
        assertTrue { events.size == 0 }
        assertTrue { transitions.size == 0 }
        counterBloc.close()
    }

    @Test
    fun CounterBloc_single_increment_event_update_state_to_1() {
        val observer = spyk(MockBlocObserver<Any>())
        BlocOverrides.runWithOverrides(observer) {
            runTest(UnconfinedTestDispatcher())
            {
                val expectedStates = listOf(1)
                val expectedTransitions =
                    listOf("Transition { currentState: 0, event: increment, nextState: 1 }")

                val events = mutableListOf<CounterEvent>()
                val transitions = mutableListOf<String>()
                val counterBloc = CounterBloc(this,
                    onEventCallback = { events.add(it) },
                    onTransitionCallback = { transition ->
                        transitions.add(transition.toString())
                    })
                counterBloc.add_sync(CounterEvent.increment)
                counterBloc.close()

                assertContentEquals(expectedTransitions, transitions)
                verify(exactly = 1) {
                    observer.onTransition(
                        counterBloc as Bloc<Any, Any>,
                        Transition(0, CounterEvent.increment, 1)
                    )
                }
                verify(exactly = 1) {
                    observer.onChange(
                        counterBloc as Bloc<Any, Any>,
                        Change(0, 1)
                    )
                }

                assertEquals(1, counterBloc.state)
            }
        }
    }

    @Test
    fun CounterBloc_multiple_increment_updates_state_to_3() {
        val observer = spyk(MockBlocObserver<Any>())
        BlocOverrides.runWithOverrides(observer) {
            runTest(UnconfinedTestDispatcher())
            {
                val expectedStates = listOf(1, 2, 3)
                val expectedTransitions = listOf(
                    "Transition { currentState: 0, event: increment, nextState: 1 }",
                    "Transition { currentState: 1, event: increment, nextState: 2 }",
                    "Transition { currentState: 2, event: increment, nextState: 3 }",
                )

                val events = mutableListOf<CounterEvent>()
                val transitions = mutableListOf<String>()
                val counterBloc = CounterBloc(this,
                    onEventCallback = { events.add(it) },
                    onTransitionCallback = { transition ->
                        transitions.add(transition.toString())
                    })
                counterBloc.add_sync(CounterEvent.increment)
                counterBloc.add_sync(CounterEvent.increment)
                counterBloc.add_sync(CounterEvent.increment)
                counterBloc.close()

                assertContentEquals(expectedTransitions, transitions)
                verify(exactly = 1) {
                    observer.onTransition(
                        counterBloc as Bloc<Any, Any>,
                        Transition(0, CounterEvent.increment, 1)
                    )
                }
                verify(exactly = 1) {
                    observer.onTransition(
                        counterBloc as Bloc<Any, Any>,
                        Transition(1, CounterEvent.increment, 2)
                    )
                }
                verify(exactly = 1) {
                    observer.onTransition(
                        counterBloc as Bloc<Any, Any>,
                        Transition(2, CounterEvent.increment, 3)
                    )
                }
                verify(exactly = 1) {
                    observer.onChange(
                        counterBloc as Bloc<Any, Any>,
                        Change(0, 1)
                    )
                }
                verify(exactly = 1) {
                    observer.onChange(
                        counterBloc as Bloc<Any, Any>,
                        Change(1, 2)
                    )
                }
                verify(exactly = 1) {
                    observer.onChange(
                        counterBloc as Bloc<Any, Any>,
                        Change(2, 3)
                    )
                }

                assertEquals(3, counterBloc.state)
            }
        }
    }

    @Test
    fun CounterBloc_is_a_broadcast_stream() = runTest(UnconfinedTestDispatcher())
    {
        //TODO: in the original dart code, expected states does not contain the initial state that is 0
        val expectedStates = listOf(0, 1)

        val counterBloc = CounterBloc(this)
        val actualStates1 = mutableListOf<Int>()
        val sub1 = async {
            counterBloc.stream.collect {
                actualStates1.add(it)
            }
        }
        val actualStates2 = mutableListOf<Int>()
        val sub2 = async {
            counterBloc.stream.collect {
                actualStates2.add(it)
            }
        }

        counterBloc.add_sync(CounterEvent.increment)
        delay(0)
        assertContentEquals(expectedStates, actualStates1)
        assertContentEquals(expectedStates, actualStates2)
        sub1.cancel()
        sub2.cancel()
        counterBloc.close()
    }

    @Test
    fun CounterBloc_multiple_subscribers_receive_the_latest_state() =
        runTest(UnconfinedTestDispatcher())
        {
            //TODO: in the original dart code, expected states does not contain the initial state that is 0
            val expectedStates = listOf(0, 1)

            val counterBloc = CounterBloc(this)
            val actualStates1 = mutableListOf<Int>()
            val sub1 = async {
                counterBloc.stream.collect {
                    actualStates1.add(it)
                }
            }
            val actualStates2 = mutableListOf<Int>()
            val sub2 = async {
                counterBloc.stream.collect {
                    actualStates2.add(it)
                }
            }
            val actualStates3 = mutableListOf<Int>()
            val sub3 = async {
                counterBloc.stream.collect {
                    actualStates3.add(it)
                }
            }

            counterBloc.add_sync(CounterEvent.increment)
            delay(0)
            assertContentEquals(expectedStates, actualStates1)
            assertContentEquals(expectedStates, actualStates2)
            assertContentEquals(expectedStates, actualStates3)
            sub1.cancel()
            sub2.cancel()
            sub3.cancel()
            counterBloc.close()
        }

    @Test
    fun CounterBloc_maintains_correct_transition_composition() = runTest(UnconfinedTestDispatcher())
    {
        val expectedTransitions = listOf(
            Transition(0, CounterEvent.decrement, -1),
            Transition(-1, CounterEvent.increment, 0)
        )
        //TODO: in the original dart code, expected states does not contain the initial state that is 0
        val expectedStates = listOf(0, -1, 0)

        val actualTransitions = mutableListOf<Transition<CounterEvent, Int>>()
        val counterBloc =
            CounterBloc(this, onTransitionCallback = { t -> actualTransitions.add(t) })
        val actualStates = mutableListOf<Int>()
        val sub1 = async {
            counterBloc.stream.collect {
                actualStates.add(it)
            }
        }

        counterBloc.add_sync(CounterEvent.decrement)
        counterBloc.add_sync(CounterEvent.increment)
        delay(0)
        assertContentEquals(expectedStates, actualStates)
        assertContentEquals(expectedTransitions, actualTransitions)
        sub1.cancel()
        counterBloc.close()
    }

    /**
     * TODO: we currently cannot reproduce this behavior of original dart code: events Are NOT processed asynchronously
     * this test fails
     */
    @Test
    fun CounterBloc_events_are_processed_asynchronously() = runTest(UnconfinedTestDispatcher()) {
        val transitions = mutableListOf<Transition<CounterEvent, Int>>()
        val events = mutableListOf<CounterEvent>()
        val counterBloc = CounterBloc(
            this,
            onTransitionCallback = { t -> transitions.add(t) },
            onEventCallback = { e -> events.add(e) })
        assertEquals(0, counterBloc.state)
        assertContentEquals(listOf(), events)
        assertContentEquals(listOf(), transitions)

        counterBloc.add(CounterEvent.increment)
        assertEquals(counterBloc.state, 0)
        assertContentEquals(events, listOf(CounterEvent.increment))
        assertContentEquals(listOf(), transitions)

        //after we wait a bit the event will already processed
        tick()
        assertEquals(counterBloc.state, 1)
        assertContentEquals(events, listOf(CounterEvent.increment))
        assertContentEquals(listOf(Transition(0, CounterEvent.increment, 1)), transitions)
        counterBloc.close()
    }

    @Test
    fun AsyncBloc_close_does_not_emit_new_states_over_the_state_stream() =
        runTest(UnconfinedTestDispatcher())
        {
            val asyncBloc = AsyncBloc(this)
            val actualStates = mutableListOf<AsyncState>()
            val sub1 = async {
                asyncBloc.stream.collect {
                    actualStates.add(it)
                }
            }
            asyncBloc.close()
            assertContentEquals(listOf(), actualStates)
        }
    /*
       setUp(() {
        asyncBloc = AsyncBloc();
        observer = MockBlocObserver();
      });

      test(
          'close while events are pending finishes processing pending events '
          'and does not trigger onError', () async {
        await BlocOverrides.runZoned(() async {
          final expectedStates = <AsyncState>[
            AsyncState.initial().copyWith(isLoading: true),
            AsyncState.initial().copyWith(isSuccess: true),
          ];
          final states = <AsyncState>[];
          final asyncBloc = AsyncBloc()
            ..stream.listen(states.add)
            ..add(AsyncEvent());

          await asyncBloc.close();

          expect(states, expectedStates);
          // ignore: invalid_use_of_protected_member
          verifyNever(() => observer.onError(any(), any(), any()));
        }, blocObserver: observer);
      });

      test('state returns correct value initially', () {
        expect(asyncBloc.state, AsyncState.initial());
      });

      test('should map single event to correct state', () {
        BlocOverrides.runZoned(() {
          final expectedStates = [
            AsyncState(isLoading: true, hasError: false, isSuccess: false),
            AsyncState(isLoading: false, hasError: false, isSuccess: true),
            emitsDone,
          ];
          final asyncBloc = AsyncBloc();

          expectLater(
            asyncBloc.stream,
            emitsInOrder(expectedStates),
          ).then((dynamic _) {
            verify(
              // ignore: invalid_use_of_protected_member
              () => observer.onTransition(
                asyncBloc,
                Transition<AsyncEvent, AsyncState>(
                  currentState: AsyncState(
                    isLoading: false,
                    hasError: false,
                    isSuccess: false,
                  ),
                  event: AsyncEvent(),
                  nextState: AsyncState(
                    isLoading: true,
                    hasError: false,
                    isSuccess: false,
                  ),
                ),
              ),
            ).called(1);
            verify(
              // ignore: invalid_use_of_protected_member
              () => observer.onChange(
                asyncBloc,
                Change<AsyncState>(
                  currentState: AsyncState(
                    isLoading: false,
                    hasError: false,
                    isSuccess: false,
                  ),
                  nextState: AsyncState(
                    isLoading: true,
                    hasError: false,
                    isSuccess: false,
                  ),
                ),
              ),
            ).called(1);
            verify(
              // ignore: invalid_use_of_protected_member
              () => observer.onTransition(
                asyncBloc,
                Transition<AsyncEvent, AsyncState>(
                  currentState: AsyncState(
                    isLoading: true,
                    hasError: false,
                    isSuccess: false,
                  ),
                  event: AsyncEvent(),
                  nextState: AsyncState(
                    isLoading: false,
                    hasError: false,
                    isSuccess: true,
                  ),
                ),
              ),
            ).called(1);
            verify(
              // ignore: invalid_use_of_protected_member
              () => observer.onChange(
                asyncBloc,
                Change<AsyncState>(
                  currentState: AsyncState(
                    isLoading: true,
                    hasError: false,
                    isSuccess: false,
                  ),
                  nextState: AsyncState(
                    isLoading: false,
                    hasError: false,
                    isSuccess: true,
                  ),
                ),
              ),
            ).called(1);
            expect(
              asyncBloc.state,
              AsyncState(
                isLoading: false,
                hasError: false,
                isSuccess: true,
              ),
            );
          });

          asyncBloc
            ..add(AsyncEvent())
            ..close();
        }, blocObserver: observer);
      });

      test('should map multiple events to correct states', () {
        BlocOverrides.runZoned(() {
          final expectedStates = [
            AsyncState(isLoading: true, hasError: false, isSuccess: false),
            AsyncState(isLoading: false, hasError: false, isSuccess: true),
            AsyncState(isLoading: true, hasError: false, isSuccess: false),
            AsyncState(isLoading: false, hasError: false, isSuccess: true),
            emitsDone,
          ];
          final asyncBloc = AsyncBloc();

          expectLater(
            asyncBloc.stream,
            emitsInOrder(expectedStates),
          ).then((dynamic _) {
            verify(
              // ignore: invalid_use_of_protected_member
              () => observer.onTransition(
                asyncBloc,
                Transition<AsyncEvent, AsyncState>(
                  currentState: AsyncState(
                    isLoading: false,
                    hasError: false,
                    isSuccess: false,
                  ),
                  event: AsyncEvent(),
                  nextState: AsyncState(
                    isLoading: true,
                    hasError: false,
                    isSuccess: false,
                  ),
                ),
              ),
            ).called(1);
            verify(
              // ignore: invalid_use_of_protected_member
              () => observer.onChange(
                asyncBloc,
                Change<AsyncState>(
                  currentState: AsyncState(
                    isLoading: false,
                    hasError: false,
                    isSuccess: false,
                  ),
                  nextState: AsyncState(
                    isLoading: true,
                    hasError: false,
                    isSuccess: false,
                  ),
                ),
              ),
            ).called(1);
            verify(
              // ignore: invalid_use_of_protected_member
              () => observer.onTransition(
                asyncBloc,
                Transition<AsyncEvent, AsyncState>(
                  currentState: AsyncState(
                    isLoading: true,
                    hasError: false,
                    isSuccess: false,
                  ),
                  event: AsyncEvent(),
                  nextState: AsyncState(
                    isLoading: false,
                    hasError: false,
                    isSuccess: true,
                  ),
                ),
              ),
            ).called(2);
            verify(
              // ignore: invalid_use_of_protected_member
              () => observer.onChange(
                asyncBloc,
                Change<AsyncState>(
                  currentState: AsyncState(
                    isLoading: true,
                    hasError: false,
                    isSuccess: false,
                  ),
                  nextState: AsyncState(
                    isLoading: false,
                    hasError: false,
                    isSuccess: true,
                  ),
                ),
              ),
            ).called(2);
            expect(
              asyncBloc.state,
              AsyncState(
                isLoading: false,
                hasError: false,
                isSuccess: true,
              ),
            );
          });

          asyncBloc
            ..add(AsyncEvent())
            ..add(AsyncEvent())
            ..close();
        }, blocObserver: observer);
      });

      test('is a broadcast stream', () {
        final expectedStates = [
          AsyncState(isLoading: true, hasError: false, isSuccess: false),
          AsyncState(isLoading: false, hasError: false, isSuccess: true),
          emitsDone,
        ];

        expect(asyncBloc.stream.isBroadcast, isTrue);
        expectLater(asyncBloc.stream, emitsInOrder(expectedStates));
        expectLater(asyncBloc.stream, emitsInOrder(expectedStates));

        asyncBloc
          ..add(AsyncEvent())
          ..close();
      });

      test('multiple subscribers receive the latest state', () {
        final expected = <AsyncState>[
          AsyncState(isLoading: true, hasError: false, isSuccess: false),
          AsyncState(isLoading: false, hasError: false, isSuccess: true),
        ];

        expectLater(asyncBloc.stream, emitsInOrder(expected));
        expectLater(asyncBloc.stream, emitsInOrder(expected));
        expectLater(asyncBloc.stream, emitsInOrder(expected));

        asyncBloc.add(AsyncEvent());
      });
    });

     */
    /**
     * TODO currently MergeBloc is not fully implemented
     */
    @Test
    fun MergeBloc_maintains_correct_transition_composition() = runTest(UnconfinedTestDispatcher()) {
        val expectedTransitions = listOf(
            Transition(0, CounterEvent.increment, 1),
            Transition(1, CounterEvent.decrement, 0),
            Transition(0, CounterEvent.decrement, -1)
        )
        val expectedStates = listOf(1, 0, -1)

        val states = mutableListOf<Int>()
        val transitions = mutableListOf<Transition<CounterEvent, Int>>()

        val bloc = MergeBloc(this, onTransitionCallback = { transitions.add(it) })
        with(bloc) {
            add_sync(CounterEvent.increment)
            add_sync(CounterEvent.increment)
            add_sync(CounterEvent.decrement)
            add_sync(CounterEvent.decrement)
            close()
        }
        assertContentEquals(expectedStates, states)
        assertContentEquals(expectedTransitions, transitions)
    }

    @Test
    fun SeededBloc_does_not_emit_repeated_states() = runTest(UnconfinedTestDispatcher()) {
        val seededBloc = SeededBloc(0, listOf(1, 2, 1, 1), this)
        //NOTE: it is listOf(1,2,1) in the original code: initial state is ignored
        val expectedStates = listOf(0, 1, 2, 1)
        val actualStates = mutableListOf<Int>()
        val sub1 = async {
            seededBloc.stream.collect {
                actualStates.add(it)
            }
        }
        seededBloc.add_sync("event")
        seededBloc.close()
        sub1.cancel()
        assertContentEquals(expectedStates, actualStates)
    }

    @Test
    fun SeededBloc_can_emit_initial_state_only_once() = runTest(UnconfinedTestDispatcher()) {
        val seededBloc = SeededBloc(0, listOf(0, 0), this)
        val expectedStates = listOf(0)
        val actualStates = mutableListOf<Int>()
        val sub1 = async {
            seededBloc.stream.collect {
                actualStates.add(it)
            }
        }
        seededBloc.add_sync("event")
        seededBloc.close()
        sub1.cancel()
        assertContentEquals(expectedStates, actualStates)
    }

    @Test
    fun SeededBloc_can_emit_initial_state_and_continue_emitting_distinct_states() =
        runTest(UnconfinedTestDispatcher()) {
            val seededBloc = SeededBloc(0, listOf(0, 0, 1), this)
            val expectedStates = listOf(0, 1)
            val actualStates = mutableListOf<Int>()
            val sub1 = async {
                seededBloc.stream.collect {
                    actualStates.add(it)
                }
            }
            seededBloc.add_sync("event")
            seededBloc.close()
            sub1.cancel()
            assertContentEquals(expectedStates, actualStates)
        }

    @Test
    fun SeededBloc_discard_subsequent_duplicate_states_for_distinct_events() =
        runTest(UnconfinedTestDispatcher()) {
            val seededBloc = SeededBloc(0, listOf(1, 1), this)
            //NOTE: it is listOf(1) in the original code: initial state is ignored
            val expectedStates = listOf(0, 1)
            val actualStates = mutableListOf<Int>()
            val sub1 = async {
                seededBloc.stream.collect {
                    actualStates.add(it)
                }
            }
            with(seededBloc) {
                add_sync("eventA")
                add_sync("eventB")
                add_sync("eventC")
                close()

            }
            sub1.cancel()
            assertContentEquals(expectedStates, actualStates)
        }

    @Test
    fun SeededBloc_discard_subsequent_duplicate_states_for_same_event() =
        runTest(UnconfinedTestDispatcher()) {
            val seededBloc = SeededBloc(0, listOf(1, 1), this)
            //NOTE: it is listOf(1) in the original code: initial state is ignored
            val expectedStates = listOf(0, 1)
            val actualStates = mutableListOf<Int>()
            val sub1 = async {
                seededBloc.stream.collect {
                    actualStates.add(it)
                }
            }
            with(seededBloc) {
                add_sync("event")
                add_sync("event")
                add_sync("event")
                close()

            }
            sub1.cancel()
            assertContentEquals(expectedStates, actualStates)
        }

    @Test
    fun StreamBloc_cancels_subscriptions_correctly() = runTest(UnconfinedTestDispatcher())
    {
        val expectedStates = listOf(0, 1, 2, 3, 4)
        val states = mutableListOf<Int>()
        val eventflow = MutableSharedFlow<Int>()
        val bloc = StreamBloc(this, eventflow)

        val sub1 = async {
            bloc.stream.collect {
                states.add(it)
            }
        }
        bloc.add_sync(Subscribe())
        tick()

        with(eventflow) {
            emit(0)
            emit(1)
            emit(2)
        }
        //TODO in the original code here there is delay(0) why we need to wait to wait so much to make this work?
        delay(120)
        bloc.add_sync(Subscribe())
        tick()
        with(eventflow) {
            emit(3)
            emit(4)
        }
        delay(300)

        bloc.close()
        sub1.cancel()
        assertContentEquals(expectedStates, states)
    }

    //TODO this test is not currently working. the problem seems to be current implementation
    // of emitter.forEach: it is currently defined as a suspend function but should instead be defined as
    // an asyc function that return a deferred, like in the original code? also handleEvent is not called
    //also another problem is that "controller" flow  does not ever complete, and without completing the
    // check that should throw blocError that is expected, will not be ever thrown
    //when bloc.add(UnawaitedForEach()) line is run
    @Test
    fun RestartableStreamEvent_unawaited_forEach_throws_Assertion_error() =
        runTest {
            var blocError: Throwable? = null
            try {
                val controller = MutableSharedFlow<Int>()
                val bloc = RestartableStreamBloc(this, controller)

                //--the following code was not in original test code
                val states = mutableListOf<Int>()
                val sub1 = async {
                    bloc.stream.collect {
                        states.add(it)
                    }
                }
                //--the previous code was not in original test code

                bloc.add_sync(UnawaitedForEach())
                tick()
                controller.emit(0)
                tick()
                delay(300) //it was 300 in the original code
                println(states) //this line was not in original test code
                sub1.cancel() //this line was not in original test code
                bloc.close()
            } catch (e: Throwable) {
                blocError = e
            }
            assertTrue { blocError != null }
            assertTrue {
                blocError?.message?.contains("An event handler completed but left pending subscriptions behind.")
                    ?: true
            }

        }

    @Test
    fun RestartableStreamBloc_unawaited_forEach_throws_AssertionError() {
        runTest {
            TODO()
        }
    }
}
/*
import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:mocktail/mocktail.dart';
import 'package:test/test.dart';

import 'blocs/blocs.dart';

Future<void> tick() => Future<void>.delayed(Duration.zero);

class MockBlocObserver extends Mock implements BlocObserver {}

class FakeBlocBase<S> extends Fake implements BlocBase<S> {}

void main() {
  group('Bloc Tests', () {


      setUpAll(() {
        registerFallbackValue(FakeBlocBase<dynamic>());
        registerFallbackValue(StackTrace.empty);
      });

    group('RestartableStreamBloc', () {
      test('unawaited forEach throws AssertionError', () async {
        late final Object blocError;
        await runZonedGuarded(() async {
          final controller = StreamController<int>.broadcast();
          final bloc = RestartableStreamBloc(controller.stream)
            ..add(UnawaitedForEach());

          await tick();

          controller.add(0);

          await tick();

          await Future<void>.delayed(const Duration(milliseconds: 300));

          await bloc.close();
        }, (error, stackTrace) {
          blocError = error;
        });

        expect(
          blocError,
          isA<AssertionError>().having(
            (e) => e.message,
            'message',
            contains(
              '''An event handler completed but left pending subscriptions behind.''',
            ),
          ),
        );
      });

      test('forEach cancels subscriptions correctly', () async {
        const expectedStates = [0, 1, 2, 3, 4];
        final states = <int>[];
        final controller = StreamController<int>.broadcast();
        final bloc = RestartableStreamBloc(controller.stream)
          ..stream.listen(states.add)
          ..add(ForEach());

        await tick();

        controller
          ..add(0)
          ..add(1)
          ..add(2);

        await tick();

        bloc.add(ForEach());

        await tick();

        controller
          ..add(3)
          ..add(4);

        await bloc.close();
        expect(states, equals(expectedStates));
      });

      test(
          'forEach with onError handles errors emitted by stream '
          'and does not cancel the subscription', () async {
        const expectedStates = [1, 2, 3, -1, 4, 5, 6];
        final error = Exception('oops');
        final states = <int>[];
        final controller = StreamController<int>.broadcast();
        final bloc = RestartableStreamBloc(controller.stream)
          ..stream.listen(states.add)
          ..add(ForEachOnError());

        await tick();

        controller
          ..add(1)
          ..add(2)
          ..add(3);

        await tick();

        controller
          ..addError(error)
          ..add(4)
          ..add(5)
          ..add(6);

        await tick();

        expect(states, equals(expectedStates));

        await bloc.close();
      });

      test('forEach with try/catch handles errors emitted by stream', () async {
        const expectedStates = [1, 2, 3, -1];
        final error = Exception('oops');
        final states = <int>[];
        final controller = StreamController<int>.broadcast();
        final bloc = RestartableStreamBloc(controller.stream)
          ..stream.listen(states.add)
          ..add(ForEachTryCatch());

        await tick();

        controller
          ..add(1)
          ..add(2)
          ..add(3);

        await tick();

        controller.addError(error);

        await tick();

        expect(states, equals(expectedStates));

        await bloc.close();
      });

      test(
          'forEach with catchError '
          'handles errors emitted by stream', () async {
        const expectedStates = [1, 2, 3, -1];
        final error = Exception('oops');
        final states = <int>[];
        final controller = StreamController<int>.broadcast();
        final bloc = RestartableStreamBloc(controller.stream)
          ..stream.listen(states.add)
          ..add(ForEachCatchError());

        await tick();

        controller
          ..add(1)
          ..add(2)
          ..add(3);

        await tick();

        controller.addError(error);

        await tick();

        expect(states, equals(expectedStates));

        await bloc.close();
      });

      test('forEach throws when stream emits error', () async {
        const expectedStates = [1, 2, 3];
        final error = Exception('oops');
        final states = <int>[];
        late final dynamic uncaughtError;

        await runZonedGuarded(() async {
          final controller = StreamController<int>.broadcast();
          final bloc = RestartableStreamBloc(controller.stream)
            ..stream.listen(states.add)
            ..add(ForEach());

          await tick();

          controller
            ..add(1)
            ..add(2)
            ..add(3);

          await tick();

          controller
            ..addError(error)
            ..add(3)
            ..add(4)
            ..add(5);

          await bloc.close();
        }, (error, stackTrace) => uncaughtError = error);
        expect(states, equals(expectedStates));
        expect(uncaughtError, equals(error));
      });

      test('unawaited onEach throws AssertionError', () async {
        late final Object blocError;
        await runZonedGuarded(() async {
          final controller = StreamController<int>.broadcast();
          final bloc = RestartableStreamBloc(controller.stream)
            ..add(UnawaitedOnEach());

          await bloc.close();
        }, (error, stackTrace) {
          blocError = error;
        });

        expect(
          blocError,
          isA<AssertionError>().having(
            (e) => e.message,
            'message',
            contains(
              '''An event handler completed but left pending subscriptions behind.''',
            ),
          ),
        );
      });

      test(
          'onEach with onError handles errors emitted by stream '
          'and does not cancel subscription', () async {
        const expectedStates = [1, 2, 3, -1, 4, 5, 6];
        final error = Exception('oops');
        final states = <int>[];
        final controller = StreamController<int>.broadcast();
        final bloc = RestartableStreamBloc(controller.stream)
          ..stream.listen(states.add)
          ..add(OnEachOnError());

        await tick();

        controller
          ..add(1)
          ..add(2)
          ..add(3);

        await tick();
        await Future<void>.delayed(const Duration(milliseconds: 300));

        controller
          ..addError(error)
          ..add(4)
          ..add(5)
          ..add(6);
        await tick();
        await Future<void>.delayed(const Duration(milliseconds: 300));

        expect(states, equals(expectedStates));

        await bloc.close();
      });

      test('onEach with try/catch handles errors emitted by stream', () async {
        const expectedStates = [1, 2, 3, -1];
        final error = Exception('oops');
        final states = <int>[];
        final controller = StreamController<int>.broadcast();
        final bloc = RestartableStreamBloc(controller.stream)
          ..stream.listen(states.add)
          ..add(OnEachTryCatch());

        await tick();

        controller
          ..add(1)
          ..add(2)
          ..add(3);

        await tick();
        await Future<void>.delayed(const Duration(milliseconds: 300));

        controller.addError(error);
        await tick();

        expect(states, equals(expectedStates));

        await bloc.close();
      });

      test(
          'onEach with try/catch handles errors '
          'emitted by stream and cancels delayed emits', () async {
        const expectedStates = [-1];
        final error = Exception('oops');
        final states = <int>[];
        final controller = StreamController<int>.broadcast();
        final bloc = RestartableStreamBloc(controller.stream)
          ..stream.listen(states.add)
          ..add(OnEachTryCatchAbort());

        await tick();

        controller
          ..add(1)
          ..add(2)
          ..add(3)
          ..addError(error);

        await tick();
        await Future<void>.delayed(const Duration(milliseconds: 300));

        expect(states, equals(expectedStates));

        await bloc.close();
      });

      test(
          'onEach with catchError '
          'handles errors emitted by stream', () async {
        const expectedStates = [1, 2, 3, -1];
        final error = Exception('oops');
        final states = <int>[];
        final controller = StreamController<int>.broadcast();
        final bloc = RestartableStreamBloc(controller.stream)
          ..stream.listen(states.add)
          ..add(OnEachCatchError());

        await tick();

        controller
          ..add(1)
          ..add(2)
          ..add(3);

        await tick();
        await Future<void>.delayed(const Duration(milliseconds: 300));

        controller.addError(error);
        await tick();

        expect(states, equals(expectedStates));

        await bloc.close();
      });

      test('onEach cancels subscriptions correctly', () async {
        const expectedStates = [3, 4];
        final states = <int>[];
        final controller = StreamController<int>.broadcast();
        final bloc = RestartableStreamBloc(controller.stream)
          ..stream.listen(states.add)
          ..add(OnEach());

        await tick();

        controller
          ..add(0)
          ..add(1)
          ..add(2);

        bloc.add(OnEach());
        await tick();

        controller
          ..add(3)
          ..add(4);

        await Future<void>.delayed(const Duration(milliseconds: 300));

        await bloc.close();
        expect(states, equals(expectedStates));
      });

      test('onEach throws when stream emits error', () async {
        const expectedStates = [1, 2, 3];
        final error = Exception('oops');
        final states = <int>[];
        late final dynamic uncaughtError;

        await runZonedGuarded(() async {
          final controller = StreamController<int>.broadcast();
          final bloc = RestartableStreamBloc(controller.stream)
            ..stream.listen(states.add)
            ..add(OnEach());

          await tick();

          controller
            ..add(1)
            ..add(2)
            ..add(3);

          await tick();
          await Future<void>.delayed(const Duration(milliseconds: 300));

          controller
            ..addError(error)
            ..add(4)
            ..add(5)
            ..add(6);

          await tick();
          await Future<void>.delayed(const Duration(milliseconds: 300));

          await bloc.close();
        }, (error, stack) => uncaughtError = error);

        expect(states, equals(expectedStates));
        expect(uncaughtError, equals(error));
      });
    });

    group('UnawaitedBloc', () {
      test(
          'throws AssertionError when emit is called '
          'after the event handler completed normally', () async {
        late final Object blocError;
        await runZonedGuarded(() async {
          final completer = Completer<void>();
          final bloc = UnawaitedBloc(completer.future)..add(UnawaitedEvent());

          await tick();

          completer.complete();

          await tick();

          await bloc.close();
        }, (error, stackTrace) => blocError = error);

        expect(
          blocError,
          isA<AssertionError>().having(
            (e) => e.message,
            'message',
            contains(
              'emit was called after an event handler completed normally.',
            ),
          ),
        );
      });
    });

    group('Exception', () {
      test('does not break stream', () {
        runZonedGuarded(() {
          final expectedStates = [-1, emitsDone];
          final counterBloc = CounterExceptionBloc();

          expectLater(counterBloc.stream, emitsInOrder(expectedStates));

          counterBloc
            ..add(CounterEvent.increment)
            ..add(CounterEvent.decrement)
            ..close();
        }, (Object error, StackTrace stackTrace) {
          expect(error.toString(), equals('Exception: fatal exception'));
          expect(stackTrace, isNotNull);
          expect(stackTrace, isNot(StackTrace.empty));
        });
      });

      test('addError triggers onError', () async {
        final expectedError = Exception('fatal exception');

        runZonedGuarded(() {
          OnExceptionBloc(
            exception: expectedError,
            onErrorCallback: (Object _, StackTrace __) {},
            // ignore: invalid_use_of_protected_member
          )..addError(expectedError, StackTrace.current);
        }, (Object error, StackTrace stackTrace) {
          expect(error, equals(expectedError));
          expect(stackTrace, isNotNull);
          expect(stackTrace, isNot(StackTrace.empty));
        });
      });

      test('triggers onError from on<E>', () {
        final exception = Exception('fatal exception');
        runZonedGuarded(() {
          Object? expectedError;
          StackTrace? expectedStacktrace;

          final onExceptionBloc = OnExceptionBloc(
            exception: exception,
            onErrorCallback: (Object error, StackTrace stackTrace) {
              expectedError = error;
              expectedStacktrace = stackTrace;
            },
          );

          expectLater(
            onExceptionBloc.stream,
            emitsInOrder(<Matcher>[emitsDone]),
          ).then((dynamic _) {
            expect(expectedError, exception);
            expect(expectedStacktrace, isNotNull);
            expect(expectedStacktrace, isNot(StackTrace.empty));
          });

          onExceptionBloc
            ..add(CounterEvent.increment)
            ..close();
        }, (Object error, StackTrace stackTrace) {
          expect(error, equals(exception));
          expect(stackTrace, isNotNull);
          expect(stackTrace, isNot(StackTrace.empty));
        });
      });

      test('triggers onError from onEvent', () {
        final exception = Exception('fatal exception');
        runZonedGuarded(() {
          OnEventErrorBloc(exception: exception)
            ..add(CounterEvent.increment)
            ..close();
        }, (Object error, StackTrace stackTrace) {
          expect(error, equals(exception));
          expect(stackTrace, isNotNull);
          expect(stackTrace, isNot(StackTrace.empty));
        });
      });

      test(
          'add throws StateError and triggers onError '
          'when bloc is closed', () {
        Object? capturedError;
        StackTrace? capturedStacktrace;
        var didThrow = false;
        runZonedGuarded(() {
          final counterBloc = CounterBloc(
            onErrorCallback: (error, stackTrace) {
              capturedError = error;
              capturedStacktrace = stackTrace;
            },
          );

          expectLater(
            counterBloc.stream,
            emitsInOrder(<Matcher>[emitsDone]),
          );

          counterBloc
            ..close()
            ..add(CounterEvent.increment);
        }, (Object error, StackTrace stackTrace) {
          didThrow = true;
          expect(error, equals(capturedError));
          expect(stackTrace, equals(capturedStacktrace));
        });

        expect(didThrow, isTrue);
        expect(
          capturedError,
          isA<StateError>().having(
            (e) => e.message,
            'message',
            'Cannot add new events after calling close',
          ),
        );
        expect(capturedStacktrace, isNotNull);
      });
    });

    group('Error', () {
      test('does not break stream', () {
        runZonedGuarded(
          () {
            final expectedStates = [-1, emitsDone];
            final counterBloc = CounterErrorBloc();

            expectLater(counterBloc.stream, emitsInOrder(expectedStates));

            counterBloc
              ..add(CounterEvent.increment)
              ..add(CounterEvent.decrement)
              ..close();
          },
          (Object _, StackTrace __) {},
        );
      });

      test('triggers onError from mapEventToState', () {
        runZonedGuarded(
          () {
            final error = Error();
            Object? expectedError;
            StackTrace? expectedStacktrace;

            final onErrorBloc = OnErrorBloc(
              error: error,
              onErrorCallback: (Object error, StackTrace stackTrace) {
                expectedError = error;
                expectedStacktrace = stackTrace;
              },
            );

            expectLater(
              onErrorBloc.stream,
              emitsInOrder(<Matcher>[emitsDone]),
            ).then((dynamic _) {
              expect(expectedError, error);
              expect(expectedStacktrace, isNotNull);
            });

            onErrorBloc
              ..add(CounterEvent.increment)
              ..close();
          },
          (Object _, StackTrace __) {},
        );
      });

      test('triggers onError from onTransition', () {
        runZonedGuarded(
          () {
            final error = Error();
            Object? expectedError;
            StackTrace? expectedStacktrace;

            final onTransitionErrorBloc = OnTransitionErrorBloc(
              error: error,
              onErrorCallback: (Object error, StackTrace stackTrace) {
                expectedError = error;
                expectedStacktrace = stackTrace;
              },
            );

            expectLater(
              onTransitionErrorBloc.stream,
              emitsInOrder(<Matcher>[emitsDone]),
            ).then((dynamic _) {
              expect(expectedError, error);
              expect(expectedStacktrace, isNotNull);
              expect(onTransitionErrorBloc.state, 0);
            });

            onTransitionErrorBloc
              ..add(CounterEvent.increment)
              ..close();
          },
          (Object _, StackTrace __) {},
        );
      });
    });

    group('emit', () {
      test('updates the state', () async {
        final counterBloc = CounterBloc();
        unawaited(
          expectLater(counterBloc.stream, emitsInOrder(const <int>[42])),
        );
        counterBloc.emit(42);
        expect(counterBloc.state, 42);
        await counterBloc.close();
      });

      test(
          'throws StateError and triggers onError '
          'when bloc is closed', () async {
        Object? capturedError;
        StackTrace? capturedStacktrace;

        final states = <int>[];
        final expectedStateError = isA<StateError>().having(
          (e) => e.message,
          'message',
          'Cannot emit new states after calling close',
        );

        final counterBloc = CounterBloc(
          onErrorCallback: (error, stackTrace) {
            capturedError = error;
            capturedStacktrace = stackTrace;
          },
        )..stream.listen(states.add);

        await counterBloc.close();

        expect(counterBloc.isClosed, isTrue);
        expect(counterBloc.state, equals(0));
        expect(states, isEmpty);
        expect(() => counterBloc.emit(1), throwsA(expectedStateError));
        expect(counterBloc.state, equals(0));
        expect(states, isEmpty);
        expect(capturedError, expectedStateError);
        expect(capturedStacktrace, isNotNull);
      });
    });

    group('close', () {
      test('emits done (sync)', () {
        final bloc = CounterBloc()..close();
        expect(bloc.stream, emitsDone);
      });

      test('emits done (async)', () async {
        final bloc = CounterBloc();
        await bloc.close();
        expect(bloc.stream, emitsDone);
      });
    });

    group('isClosed', () {
      test('returns true after bloc is closed', () async {
        final bloc = CounterBloc();
        expect(bloc.isClosed, isFalse);
        await bloc.close();
        expect(bloc.isClosed, isTrue);
      });
    });
  });
}

void unawaited(Future<void> future) {}

 */