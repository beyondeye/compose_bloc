package cafe.adriel.voyager.core.registry

import cafe.adriel.voyager.core.screen.Screen
import kotlin.reflect.KClass
import cafe.adriel.voyager.core.concurrent.ThreadSafeMap

private typealias ProviderKey = KClass<out ScreenProvider>

private typealias ScreenFactory = (ScreenProvider) -> Screen

/**
 * see https://voyager.adriel.cafe/navigation/multi-module-navigation
 */
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
