package com.beyondeye.kbloc.compose.registry

import com.beyondeye.kbloc.compose.concurrent.ThreadSafeMap
import com.beyondeye.kbloc.compose.screen.Screen
import kotlin.reflect.KClass

private typealias ProviderKey = KClass<out ScreenProvider>

private typealias ScreenFactory = (ScreenProvider) -> Screen

public object ScreenRegistry {

    @PublishedApi
    internal val factories: ThreadSafeMap<ProviderKey, ScreenFactory> = ThreadSafeMap()

    public operator fun invoke(block: ScreenRegistry.() -> Unit) {
        this.block()
    }

    public inline fun <reified T : ScreenProvider> register(noinline factory: (T) -> Screen) {
        factories[T::class] = factory as ScreenFactory
    }

    public fun get(provider: ScreenProvider): Screen {
        val factory = factories[provider::class]
            ?: error("ScreenProvider not registered: ${provider::class.qualifiedName}")
        return factory(provider)
    }
}
