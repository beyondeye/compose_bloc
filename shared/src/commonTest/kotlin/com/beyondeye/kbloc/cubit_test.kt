package com.beyondeye.kbloc

import com.beyondeye.kbloc.core.*
import com.beyondeye.kbloc.cubits.CounterCubit
import io.mockk.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockBlocObserver<T:Any> : BlocObserver<T> {}

class CubitTests {
    @BeforeTest
    //see https://sonique6784.medium.com/pure-kotlin-unit-testing-mocking-part-2-e13857014ea6
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)
    @Test
    fun constructor_triggers_onCreate_on_observer() {
        val observer= spyk<MockBlocObserver<Any>>()
        BlocOverrides.runZoned(blocObserver = observer) {
            val cubit = CounterCubit()
            //see https://mockk.io/#verification-atleast-atmost-or-exactly-times
            verify(exactly =1) { observer.onCreate(cubit as BlocBase<Any>) }
        }
    }
    @Test
    fun initial_state_is_correct() {
        val cubit=CounterCubit()
        assertEquals(0,cubit.state)
    }

    @Test
    fun addError_triggers_onError() {
        val observer= spyk<MockBlocObserver<Any>>()
        val expectedError= Exception("fatal exception")
        val errors= mutableListOf<Throwable>()
        BlocOverrides.runZoned(blocObserver = observer) {
            val cubit = CounterCubit(onErrorCallback = {e-> errors.add(e)})
            cubit.addError(expectedError)

            assertEquals(1,errors.size)
            assertEquals(expectedError,errors[0])
            verify(exactly =1) { observer.onError(cubit as BlocBase<Any>,expectedError) }
        }

    }
}

/*
class MockBlocObserver extends Mock implements BlocObserver {}
class FakeBlocBase<S> extends Fake implements BlocBase<S> {}
class FakeChange<S> extends Fake implements Change<S> {}

void main() {
  group('Cubit', () {



    group('onChange', () {
      late BlocObserver observer;

      setUpAll(() {
        registerFallbackValue(FakeBlocBase<dynamic>());
        registerFallbackValue(FakeChange<dynamic>());
      });

      setUp(() {
        observer = MockBlocObserver();
      });

      test('is not called for the initial state', () async {
        await BlocOverrides.runZoned(() async {
          final changes = <Change<int>>[];
          final cubit = CounterCubit(onChangeCallback: changes.add);
          await cubit.close();
          expect(changes, isEmpty);
          // ignore: invalid_use_of_protected_member
          verifyNever(() => observer.onChange(any(), any()));
        }, blocObserver: observer);
      });

      test('is called with correct change for a single state change', () async {
        await BlocOverrides.runZoned(() async {
          final changes = <Change<int>>[];
          final cubit = CounterCubit(onChangeCallback: changes.add)
            ..increment();
          await cubit.close();
          expect(
            changes,
            const [Change<int>(currentState: 0, nextState: 1)],
          );
          verify(
            // ignore: invalid_use_of_protected_member
            () => observer.onChange(
              cubit,
              const Change<int>(currentState: 0, nextState: 1),
            ),
          ).called(1);
        }, blocObserver: observer);
      });

      test('is called with correct changes for multiple state changes',
          () async {
        await BlocOverrides.runZoned(() async {
          final changes = <Change<int>>[];
          final cubit = CounterCubit(onChangeCallback: changes.add)
            ..increment()
            ..increment();
          await cubit.close();
          expect(
            changes,
            const [
              Change<int>(currentState: 0, nextState: 1),
              Change<int>(currentState: 1, nextState: 2),
            ],
          );
          verify(
            // ignore: invalid_use_of_protected_member
            () => observer.onChange(
              cubit,
              const Change<int>(currentState: 0, nextState: 1),
            ),
          ).called(1);
          verify(
            // ignore: invalid_use_of_protected_member
            () => observer.onChange(
              cubit,
              const Change<int>(currentState: 1, nextState: 2),
            ),
          ).called(1);
        }, blocObserver: observer);
      });
    });

    group('emit', () {
      test('throws StateError if cubit is closed', () {
        var didThrow = false;
        runZonedGuarded(() {
          final cubit = CounterCubit();
          expectLater(
            cubit.stream,
            emitsInOrder(<Matcher>[equals(1), emitsDone]),
          );
          cubit
            ..increment()
            ..close()
            ..increment();
        }, (error, _) {
          didThrow = true;
          expect(
            error,
            isA<StateError>().having(
              (e) => e.message,
              'message',
              'Cannot emit new states after calling close',
            ),
          );
        });
        expect(didThrow, isTrue);
      });

      test('emits states in the correct order', () async {
        final states = <int>[];
        final cubit = CounterCubit();
        final subscription = cubit.stream.listen(states.add);
        cubit.increment();
        await cubit.close();
        await subscription.cancel();
        expect(states, [1]);
      });

      test('can emit initial state only once', () async {
        final states = <int>[];
        final cubit = SeededCubit(initialState: 0);
        final subscription = cubit.stream.listen(states.add);
        cubit
          ..emitState(0)
          ..emitState(0);
        await cubit.close();
        await subscription.cancel();
        expect(states, [0]);
      });

      test(
          'can emit initial state and '
          'continue emitting distinct states', () async {
        final states = <int>[];
        final cubit = SeededCubit(initialState: 0);
        final subscription = cubit.stream.listen(states.add);
        cubit
          ..emitState(0)
          ..emitState(1);
        await cubit.close();
        await subscription.cancel();
        expect(states, [0, 1]);
      });

      test('does not emit duplicate states', () async {
        final states = <int>[];
        final cubit = SeededCubit(initialState: 0);
        final subscription = cubit.stream.listen(states.add);
        cubit
          ..emitState(1)
          ..emitState(1)
          ..emitState(2)
          ..emitState(2)
          ..emitState(3)
          ..emitState(3);
        await cubit.close();
        await subscription.cancel();
        expect(states, [1, 2, 3]);
      });
    });

    group('listen', () {
      test('returns a StreamSubscription', () {
        final cubit = CounterCubit();
        final subscription = cubit.stream.listen((_) {});
        expect(subscription, isA<StreamSubscription<int>>());
        subscription.cancel();
        cubit.close();
      });

      test('does not receive current state upon subscribing', () async {
        final states = <int>[];
        final cubit = CounterCubit()..stream.listen(states.add);
        await cubit.close();
        expect(states, isEmpty);
      });

      test('receives single async state', () async {
        final states = <int>[];
        final cubit = FakeAsyncCounterCubit()..stream.listen(states.add);
        await cubit.increment();
        await cubit.close();
        expect(states, [equals(1)]);
      });

      test('receives multiple async states', () async {
        final states = <int>[];
        final cubit = FakeAsyncCounterCubit()..stream.listen(states.add);
        await cubit.increment();
        await cubit.increment();
        await cubit.increment();
        await cubit.close();
        expect(states, [equals(1), equals(2), equals(3)]);
      });

      test('can call listen multiple times', () async {
        final states = <int>[];
        final cubit = CounterCubit()
          ..stream.listen(states.add)
          ..stream.listen(states.add)
          ..increment();
        await cubit.close();
        expect(states, [equals(1), equals(1)]);
      });
    });

    group('close', () {
      late MockBlocObserver observer;

      setUp(() {
        observer = MockBlocObserver();
      });

      test('triggers onClose on observer', () async {
        await BlocOverrides.runZoned(() async {
          final cubit = CounterCubit();
          await cubit.close();
          // ignore: invalid_use_of_protected_member
          verify(() => observer.onClose(cubit)).called(1);
        }, blocObserver: observer);
      });

      test('emits done (sync)', () {
        final cubit = CounterCubit()..close();
        expect(cubit.stream, emitsDone);
      });

      test('emits done (async)', () async {
        final cubit = CounterCubit();
        await cubit.close();
        expect(cubit.stream, emitsDone);
      });
    });

    group('isClosed', () {
      test('returns true after cubit is closed', () async {
        final cubit = CounterCubit();
        expect(cubit.isClosed, isFalse);
        await cubit.close();
        expect(cubit.isClosed, isTrue);
      });
    });
  });
}

 */