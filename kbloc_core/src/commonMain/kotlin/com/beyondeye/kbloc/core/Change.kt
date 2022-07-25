package com.beyondeye.kbloc.core

/**
 * A [Change] represents the change from one [State] to another.
 * A [Change] consists of the [currentState] and [nextState].
 */
public open class Change<State>(
    /**
     * The current [State] at the time of the [Change].
     */
    public val currentState:State,
    /**
     * The next [State] at the time of the [Change].
     */
    public val nextState:State)
{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Change<*>

        if (currentState != other.currentState) return false
        if (nextState != other.nextState) return false

        return true
    }

    override fun hashCode(): Int {
        var result = currentState?.hashCode() ?: 0
        result = 31 * result + (nextState?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Change { currentState: $currentState, nextState: $nextState }"
    }
}

