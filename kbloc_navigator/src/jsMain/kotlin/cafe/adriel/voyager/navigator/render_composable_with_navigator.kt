package cafe.adriel.voyager.navigator

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.renderComposable
import org.jetbrains.compose.web.renderComposableInBody
import org.w3c.dom.Element
import org.w3c.dom.HTMLBodyElement
import org.w3c.dom.get

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
        _init_kbloc_for_subtree(LocalDomScope.provides(this)) {
            Navigator(screens, disposeBehavior, onBackPressed)
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