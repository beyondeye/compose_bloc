package com.beyondeye.kbloc

import com.beyondeye.kbloc.core.Change
import com.beyondeye.kbloc.core.Transition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

interface TransitionEvent
interface TransitionState
class SimpleTransitionEvent:TransitionEvent
class SimpleTransitionState:TransitionState
data class CounterEvent_(val eventData:String):TransitionEvent
data class CounterState_(val count:Int):TransitionState

class ChangeAndTransitionTests {
    @Test
    fun change_constructor_should_return_normally_when_initialized_with_all_required_parameters() {
        val c= Change<Int>(0,1)
    }
    @Test
    fun change_equal_operator_should_return_true_if_2_changes_are_equal() {
        val changeA=Change<Int>(0,1)
        val changeB=Change<Int>(0,1)
        assertEquals(changeA,changeB)
    }
    @Test
    fun change_equal_operator_should_return_false_if_2_changes_are_not_equal() {
        val changeA=Change<Int>(0,1)
        val changeB=Change<Int>(0,-1)
        assertNotEquals(changeA,changeB)
    }

    @Test
    fun transition_constructor_should_not_throw_assertion_when_initialied_with_null_state() {
        val t= Transition<TransitionEvent,TransitionState?>(null,SimpleTransitionEvent(),SimpleTransitionState())
    }

    @Test
    fun transition_constructor_should_not_throw_assertion_when_initialied_with_null_event() {
        val t= Transition<TransitionEvent?,TransitionState>(SimpleTransitionState(),null,SimpleTransitionState())
    }

    @Test
    fun transition_constructor_should_not_throw_assertion_when_initialied_with_null_nextstate() {
        val t= Transition<TransitionEvent,TransitionState?>(SimpleTransitionState(),SimpleTransitionEvent(),null)
    }
    @Test
    fun transition_constructor_should_not_throw_assertion_when_initialied_with_all_required_params() {
        val t= Transition<TransitionEvent?,TransitionState>(SimpleTransitionState(),SimpleTransitionEvent(),SimpleTransitionState())
    }

    @Test
    fun transition_equals_should_return_true_if_2_transitions_are_equal() {
        val transitionA = Transition<CounterEvent_, CounterState_>(
            CounterState_(0),
            CounterEvent_("increment"),
            CounterState_(1)
        )
        val transitionB = Transition<CounterEvent_, CounterState_>(
            CounterState_(0),
            CounterEvent_("increment"),
            CounterState_(1)
        )
        assertEquals(transitionA,transitionB)
    }

    @Test
    fun transition_equals_should_return_false_if_2_transitions_are_not_equal() {
        val transitionA = Transition<CounterEvent_, CounterState_>(
            CounterState_(0),
            CounterEvent_("increment"),
            CounterState_(1)
        )
        val transitionB = Transition<CounterEvent_, CounterState_>(
            CounterState_(1),
            CounterEvent_("decrement"),
            CounterState_(0)
        )
        assertNotEquals(transitionA,transitionB)
    }
}

/*

void main() {
  group('Change Tests', () {


    group('hashCode', () {
      test('should return correct hashCode', () {
        final change = const Change<int>(currentState: 0, nextState: 1);
        expect(
          change.hashCode,
          change.currentState.hashCode ^ change.nextState.hashCode,
        );
      });
    });

    group('toString', () {
      test('should return correct string representation of Change', () {
        final change = const Change<int>(currentState: 0, nextState: 1);

        expect(
          change.toString(),
          'Change { currentState: ${change.currentState.toString()}, '
          'nextState: ${change.nextState.toString()} }',
        );
      });
    });
  });

  group('Transition Tests', () {
    group('constructor', () {


    group('hashCode', () {
      test('should return correct hashCode', () {
        final transition = Transition<CounterEvent, CounterState>(
          currentState: CounterState(0),
          event: CounterEvent('increment'),
          nextState: CounterState(1),
        );
        expect(
          transition.hashCode,
          transition.currentState.hashCode ^
              transition.event.hashCode ^
              transition.nextState.hashCode,
        );
      });
    });

    group('toString', () {
      test('should return correct string representation for Transition', () {
        final transition = Transition<CounterEvent, CounterState>(
          currentState: CounterState(0),
          event: CounterEvent('increment'),
          nextState: CounterState(1),
        );

        expect(
            transition.toString(),
            'Transition { currentState: ${transition.currentState.toString()}, '
            'event: ${transition.event.toString()}, '
            'nextState: ${transition.nextState.toString()} }');
      });
    });
  });
}

 */