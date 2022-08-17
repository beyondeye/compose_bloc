package cafe.adriel.voyager.core.registry

private typealias ScreenModule = ScreenRegistry.() -> Unit

/**
 * see https://voyager.adriel.cafe/navigation/multi-module-navigation
 */
public fun screenModule(block: ScreenModule): ScreenModule =
    { block() }
