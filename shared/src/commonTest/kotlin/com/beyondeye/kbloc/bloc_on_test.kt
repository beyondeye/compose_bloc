package com.beyondeye.kbloc

import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.Emitter
import com.beyondeye.kbloc.core.StateError
import io.mockk.MockKAnnotations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlin.test.BeforeTest
import kotlin.test.Test
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

) : Bloc<TestEvent, TestState>(cscope, TestState())
{
    init {
        on<TestEventA>(handler = onTestEventA)
        on<TestEventB>(handler = onTestEventB)
        on<TestEventAA>(handler = onTestEventAA)
        on<TestEventBA>(handler = onTestEventBA)
        on<TestEvent>(handler = onTestEvent)
    }
}
class DuplicateHandlerBloc(cscope: CoroutineScope) : Bloc<TestEvent,TestState>(cscope, TestState()) {
    init {
        on<TestEvent> {_,_-> }
        on<TestEvent> {_,_-> }
    }
}

class MissingHandlerBloc(cscope: CoroutineScope) : Bloc<TestEvent,TestState>(cscope, TestState()) {
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

}
/*


void main() {
  group('on<Event>', () {

    test('invokes all on<T> when event E is added where E is T', () async {
      var onEventCallCount = 0;
      var onACallCount = 0;
      var onBCallCount = 0;
      var onAACallCount = 0;
      var onBACallCount = 0;

      final bloc = TestBloc(
        onTestEvent: (_, __) => onEventCallCount++,
        onTestEventA: (_, __) => onACallCount++,
        onTestEventB: (_, __) => onBCallCount++,
        onTestEventAA: (_, __) => onAACallCount++,
        onTestEventBA: (_, __) => onBACallCount++,
      )..add(TestEventA());

      await Future<void>.delayed(Duration.zero);

      expect(onEventCallCount, equals(1));
      expect(onACallCount, equals(1));
      expect(onBCallCount, equals(0));
      expect(onAACallCount, equals(0));
      expect(onBACallCount, equals(0));

      bloc.add(TestEventAA());

      await Future<void>.delayed(Duration.zero);

      expect(onEventCallCount, equals(2));
      expect(onACallCount, equals(2));
      expect(onBCallCount, equals(0));
      expect(onAACallCount, equals(1));
      expect(onBACallCount, equals(0));

      bloc.add(TestEventB());

      await Future<void>.delayed(Duration.zero);

      expect(onEventCallCount, equals(3));
      expect(onACallCount, equals(2));
      expect(onBCallCount, equals(1));
      expect(onAACallCount, equals(1));
      expect(onBACallCount, equals(0));

      bloc.add(TestEventBA());

      await Future<void>.delayed(Duration.zero);

      expect(onEventCallCount, equals(4));
      expect(onACallCount, equals(2));
      expect(onBCallCount, equals(2));
      expect(onAACallCount, equals(1));
      expect(onBACallCount, equals(1));

      await bloc.close();
    });
  });
}

 */