package cafe.adriel.voyager.core.registry

import cafe.adriel.voyager.core.screen.Screen
import kotlinx.atomicfu.AtomicRef
import kotlin.reflect.KClass
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf

private typealias ProviderKey = KClass<out ScreenProvider>

private typealias ScreenFactory = (ScreenProvider) -> Screen

/**
 * see https://voyager.adriel.cafe/navigation/multi-module-navigation
 */
public object ScreenRegistry {

    //atomicref data should not be public according to atomicfu docs
    @PublishedApi
    internal val factories: AtomicRef<PersistentMap<ProviderKey, ScreenFactory>> =
        atomic(persistentHashMapOf())

    public operator fun invoke(block: ScreenRegistry.() -> Unit) {
        this.block()
    }

    public inline fun <reified T : ScreenProvider> register(noinline factory: (T) -> Screen) {
        factories.update { it.put(T::class,factory as ScreenFactory) }
    }

    public fun get(provider: ScreenProvider): Screen {
        val factory = factories.value[provider::class]
            ?: error("ScreenProvider not registered: ${provider::class.simpleName}")
        return factory(provider)
    }
}
