package cafe.adriel.voyager.core.model

import androidx.compose.runtime.DisallowComposableCalls
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import cafe.adriel.voyager.core.concurrent.ThreadSafeMap

private typealias ScreenModelKey = String

internal typealias DependencyKey = String
private typealias DependencyInstance = Any

internal typealias DependencyOnDispose = (Any) -> Unit

internal typealias Dependency = Pair<DependencyInstance, DependencyOnDispose>

/**
 * *DARIO* made this a class instead of object
 */
public class ScreenModelStore {

    /**
     * a list of currently active [ScreenModel] instances. an active instance is an instance that
     * is created/injected with method [Screen.rememberScreenModel] that is usually called at the
     * beginning of [Screen.Content] method implementation.
     * When a [Screen] is popped from the [Navigator] stack the method [ScreenModel.onDispose] is
     * called and it is removed from this map
     */
    @PublishedApi
    internal val screenModels: MutableMap<ScreenModelKey, ScreenModel> = ThreadSafeMap()

    /**
     * a list of [Dependency] of some [ScreenModel] instance. for example a [coroutineScope]
     * associated to that specific [ScreenModel] instance. when a [Screen] is popped from the [Navigator]
     * stack the [DependencyOnDispose] method associated to the dependency is called and it is removed
     * from this map.
     * Note that a [Dependency] although it is always defined relative to some specific [ScreenModel]
     * instance, this relation is not used, but is instead used the relation to the [ScreenModel] parent
     * [Screen], in the [remove] method that is called by the [Navigator] when a screen is popped from the stack
     */
    @PublishedApi
    internal val dependencies: MutableMap<DependencyKey, Dependency> = ThreadSafeMap()

    /**
     * TODO why this is a MutableStateFlow? it is currently used only in [getDependencyKey]
     * I don't like it. Need to understand this better
     */
    @PublishedApi
    internal val lastScreenModelKey: MutableStateFlow<ScreenModelKey?> = MutableStateFlow(null)

    /**
     * key for a [ScreenModel] of a specific type T for a specific [screen], with an additional
     * optional user defined [tag] appended in the end, in case there are more than one [ScreenModel]
     * of the same type for this [Screen]
     */
    @PublishedApi
    internal inline fun <reified T : ScreenModel> getKey(screen: Screen, tag: String?): ScreenModelKey =
        "${screen.key}:${T::class.qualifiedName}:${tag ?: "default"}"

    @PublishedApi
    internal fun getDependencyKey(screenModel: ScreenModel, name: String): DependencyKey =
        screenModels
            .firstNotNullOfOrNull {
                if (it.value == screenModel) it.key
                else null
            }
            ?: lastScreenModelKey.value
                ?.let { "$it:$name" }
            ?: "standalone:$name"

    @PublishedApi
    internal inline fun <reified T : ScreenModel> getOrPut(
        screen: Screen,
        tag: String?,
        factory: @DisallowComposableCalls () -> T
    ): T {
        val key = getKey<T>(screen, tag)
        lastScreenModelKey.value = key
        return screenModels.getOrPut(key, factory) as T
    }

    /**
     * @param onDispose: method analogous to [ScreenModel.onDispose] that is called when
     * a [Screen] is popped from the [Navigator] stack and all its associated [ScreenModel]
     * and ScreenModel dependencies are disposed
     */
    public inline fun <reified T : Any> getOrPutDependency(
        screenModel: ScreenModel,
        name: String,
        noinline onDispose: @DisallowComposableCalls (T) -> Unit = {},
        noinline factory: @DisallowComposableCalls (DependencyKey) -> T
    ): T {
        val key = getDependencyKey(screenModel, name)

        return dependencies
            .getOrPut(key) { (factory(key) to onDispose) as Dependency }
            .first as T
    }
    public fun <T : Any> getDependencyOrNull(
        screenModel: ScreenModel,
        name: String,
    ): T? {
        val key = getDependencyKey(screenModel, name)
        return dependencies.get(key) as T?
    }

    /**
     * method that is called from [Navigator.dispose] when a [Screen] is popped from the navigation
     * stack
     */
    public fun remove(screen: Screen) {
        screenModels.onEach(screen) { key ->
            screenModels[key]?.onDispose()
            screenModels -= key
        }

        dependencies.onEach(screen) { key ->
            dependencies[key]?.let { (instance, onDispose) -> onDispose(instance) }
            dependencies -= key
        }
    }

    /**
     * helper method to iterate on all keys of dependencies associated to a specific [Screen]
     * This is needed because dependencies are created as associated to some [ScreenModel] instance
     * so we need to find all the [ScreenModel] associated to the screen
     */
    private fun Map<String, *>.onEach(screen: Screen, block: (String) -> Unit) =
        asSequence()
            .filter { it.key.startsWith(screen.key) }
            .map { it.key }
            .forEach(block)
}
