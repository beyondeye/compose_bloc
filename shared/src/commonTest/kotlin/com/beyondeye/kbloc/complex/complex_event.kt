
package com.beyondeye.kbloc.complex

//@immutable
abstract class ComplexEvent {}

class ComplexEventA:ComplexEvent() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        return true
    }

    override fun hashCode(): Int {
        return 0
    }
}

class ComplexEventB:ComplexEvent() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        return true
    }

    override fun hashCode(): Int {
        return 1
    }
}

class ComplexEventC:ComplexEvent() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        return true
    }

    override fun hashCode(): Int {
        return 2
    }
}

class ComplexEventD:ComplexEvent() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        return true
    }

    override fun hashCode(): Int {
        return 3
    }
}
