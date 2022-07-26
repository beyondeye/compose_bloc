package com.beyondeye.kbloc.compose.screen

import com.beyondeye.kbloc.compose.concurrent.AtomicInt32

public typealias ScreenKey = String

private val nextScreenKey = AtomicInt32(0)

public val Screen.uniqueScreenKey: ScreenKey
    get() = "Screen#${nextScreenKey.getAndIncrement()}"
