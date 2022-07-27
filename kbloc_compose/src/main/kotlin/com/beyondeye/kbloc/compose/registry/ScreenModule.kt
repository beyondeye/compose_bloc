package com.beyondeye.kbloc.compose.registry

private typealias ScreenModule = ScreenRegistry.() -> Unit

/**
 * see https://voyager.adriel.cafe/navigation/multi-module-navigation
 */
public fun screenModule(block: ScreenModule): ScreenModule =
    { block() }
