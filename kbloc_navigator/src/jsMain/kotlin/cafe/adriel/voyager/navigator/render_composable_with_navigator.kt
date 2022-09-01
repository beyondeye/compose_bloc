package cafe.adriel.voyager.navigator

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.model.ScreenModelStore
import cafe.adriel.voyager.core.model.internal.LocalScreenModelStoreOwner
import cafe.adriel.voyager.core.model.internal.ScreenModelStoreOwner
import cafe.adriel.voyager.core.screen.Screen
import com.beyondeye.kbloc.compose.internal.BlocStore
import com.beyondeye.kbloc.compose.internal.BlocStoreOwner
import com.beyondeye.kbloc.compose.internal.LocalBlocStoreOwner
import io.github.aakira.napier.Napier
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.renderComposable
import org.jetbrains.compose.web.renderComposableInBody
import org.w3c.dom.Element
import org.w3c.dom.HTMLBodyElement
import org.w3c.dom.get

internal class ElementScreenModelStoreOwner: ScreenModelStoreOwner {
    /**
     * This method will be called when this ElementScreenModelStoreOwner is no longer used and
     * will be destroyed.
     * see also implementation of [ScreenModelStore.remove]
     */
    fun onCleared() {
        //Napier.d("ElementScreenModelStoreOwner:onCleared()")
        // first dispose all dependencies
        for (entry in store.dependencies.value) {
            val (instance,onDispose)= entry.value
            onDispose(instance)
        }
        // then dispose all screenModels
        for(entry in store.screenModels.value) {
            val model=entry.value
            //Napier.d(LOGTAG,"Disposing screenmodel $model")
            model.onDispose()
        }
    }

    override val screenModelStore: ScreenModelStore
        get() = store
    private val store: ScreenModelStore = ScreenModelStore()
}
internal class ElementBlocStoreOwner : BlocStoreOwner {
    override val blocStore: BlocStore
        get() = store
    private val store: BlocStore = BlocStore()

    /**
     * This method will be called when this ElementBlocStoreOwner is no longer used and will be destroyed.
     *
     * see also implementation of [BlocStore.remove]
     */
    fun onCleared() {
        //Napier.d("ActivityBlocStoreViewModel:onCleared()")
        //first clear depedendencies
        for (entry in store.blocs_dependencies_value) {
            val (instance, onDispose) = entry.value
            onDispose(instance)
        }
        //then clear blocs
        for (entry in store.blocs_value.entries) {
            val b = entry.value
            //Napier.d("Disposing bloc $b")
            GlobalScope.async {
                b.dispose()
            }
        }
    }
}


/**
 * *DARIO*
 * The CompositionLocal containing the [DOMScope] for the [Element] used to bind composable content.
 */
public object LocalDomScope {
    private val LocalDomScope =
        compositionLocalOf<DOMScope<Element>?> { null }

    /**
     * Returns current composition local value for [DOMScope] or throw exception if one has not
     * been provided
     */
    public val current: DOMScope<Element>
        @Composable
        get() = LocalDomScope.current
            ?: throw Exception("It seem you are missing definition of a root navigator!")


    /**
     */
    public infix fun provides(domeScope: DOMScope<Element>):
            ProvidedValue<DOMScope<Element>?> {
        return LocalDomScope.provides(domeScope)
    }
}

/**
 * for js a composable can be attached to any dom element in the page.
 * with method renderComposable, so instead of defining a root navigator
 * we define custom renderComposable methods that also initialize a rootnavigator
 */
private fun <TElement : Element> renderComposableWithNavigatorImpl(
    root: TElement,
    monotonicFrameClock: MonotonicFrameClock = DefaultMonotonicFrameClock,
    screens: List<Screen>, //navigator initial nav stack (at list one screen)
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = { true } //navigator onBackPressed override
): Composition {
    if (screens.size == 0) throw IllegalArgumentException()
    val content_w_navigator: @Composable DOMScope<TElement>.() -> Unit = {
        val screenModelStore=ElementScreenModelStoreOwner()
        val blocStore=ElementBlocStoreOwner()
        CompositionLocalProvider(
            LocalDomScope.provides(this),
            LocalScreenModelStoreOwner.provides(screenModelStore),
            LocalBlocStoreOwner.provides(blocStore))
        {
            Navigator(screens, disposeBehavior, onBackPressed)
            //TODO check onDispose() is actually  triggered and when
            DisposableEffect(true) {
                onDispose {
                    Napier.d("onCleared for screenModelStore  and blocStore")
                    screenModelStore.onCleared()
                    blocStore.onCleared()
                }
            }
        }
    }
    return renderComposable(root, monotonicFrameClock, content_w_navigator)
}

/**
 * Use this method to mount the composition at the element with id - [rootElementId].
 * This method is the same as [renderComposable] but also initialize the root kbloc-navigator
 *
 * @param rootElementId - the id of the [Element] that will be the root of the DOM tree managed
 * by Compose
 * @param screens - navigator initial nav stack (at list one screen)
 *
 * @return the instance of the [Composition]
 */
@Suppress("UNCHECKED_CAST")
public fun renderComposableWithNavigator(
    rootElementId: String,
    screens: List<Screen>,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = { true }, //navigator onBackPressed override
): Composition = renderComposableWithNavigatorImpl(
    document.getElementById(rootElementId)!!,
    DefaultMonotonicFrameClock,
    screens,disposeBehavior,onBackPressed)
/**
 * Use this method to mount the composition at the [HTMLBodyElement] of the current document
 * this method is the same as [renderComposableInBody] but also initialize the root kbloc-navigator
 *
 * @param screens - navigator initial nav stack (at list one screen)
 *
 * @return the instance of the [Composition]
 */
public fun renderComposableInBodyWithNavigator(
    screens: List<Screen>, //navigator initial nav stack (at list one screen)
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = { true }, //navigator onBackPressed override
): Composition = renderComposableWithNavigatorImpl(
    document.getElementsByTagName("body")[0] as HTMLBodyElement,
    DefaultMonotonicFrameClock,
    screens,disposeBehavior,onBackPressed,
)