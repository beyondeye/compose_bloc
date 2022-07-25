package com.beyondeye.kbloc.async

data class AsyncState(val isLoading: Boolean, val hasError: Boolean, val isSuccess: Boolean) {
    override fun toString(): String {
        return "AsyncState { isLoading: $isLoading, hasError: $hasError, isSuccess: $isSuccess }"
    }

    companion object {
        fun initial() = AsyncState(false, false, false)
    }
}