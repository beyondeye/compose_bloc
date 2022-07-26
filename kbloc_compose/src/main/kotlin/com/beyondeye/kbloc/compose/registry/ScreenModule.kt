package com.beyondeye.kbloc.compose.registry

private typealias ScreenModule = ScreenRegistry.() -> Unit

public fun screenModule(block: ScreenModule): ScreenModule =
    { block() }
