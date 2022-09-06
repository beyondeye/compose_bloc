package com.beyondeye.kbloc.router

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.model.internal.LocalScreenModelStoreOwner
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.*
import com.beyondeye.kbloc.compose.internal.LocalBlocStoreOwner
import io.github.aakira.napier.Napier
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.renderComposable
import org.jetbrains.compose.web.renderComposableInBody
import org.w3c.dom.Element
import org.w3c.dom.HTMLBodyElement
import org.w3c.dom.get

//
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
        //TODO refactor common code between this method and renderComposableWithNavigatorImpl in kbloc-navigator module
        val screenModelStore= ElementScreenModelStoreOwner()
        val blocStore= ElementBlocStoreOwner()
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