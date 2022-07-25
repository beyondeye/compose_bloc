package com.beyondeye.kbloc.core

/**
 * A [Transition] is the change from one state to another.
 * Consists of the [currentState], an [event], and the [nextState].
 */
public class Transition<Event,State>(
    currentState:State,
    /**
     * The [Event] which triggered the current [Transition].
     */
    public val event:Event,
    nextState:State): Change<State>(currentState,nextState) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as Transition<*, *>

        if (event != other.event) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (event?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Transition { currentState: $currentState, event: $event, nextState: $nextState }"
    }
}
