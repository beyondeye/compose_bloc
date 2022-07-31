package com.beyondeye.kbloc.compose.navigator

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.beyondeye.kbloc.compose.bloc.internals.BlocStore
import com.beyondeye.kbloc.compose.concurrent.ThreadSafeList
import com.beyondeye.kbloc.compose.lifecycle.ScreenLifecycleStore
import com.beyondeye.kbloc.compose.lifecycle.rememberScreenLifecycleOwner
import com.beyondeye.kbloc.compose.model.ScreenModelStore
import com.beyondeye.kbloc.compose.navigator.internals.*
import com.beyondeye.kbloc.compose.navigator.internals.LocalNavigatorStateHolder
import com.beyondeye.kbloc.compose.navigator.internals.NavigatorDisposableEffect
import com.beyondeye.kbloc.compose.navigator.internals.StepDisposableEffect
import com.beyondeye.kbloc.compose.navigator.internals.rememberNavigator
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.compose.stack.Stack
import com.beyondeye.kbloc.compose.stack.toMutableStateStack


public typealias NavigatorContent = @Composable (navigator: Navigator) -> Unit

public typealias OnBackPressed = ((currentScreen: Screen) -> Boolean)?

public val LocalNavigator: ProvidableCompositionLocal<Navigator?> =
    staticCompositionLocalOf { null }

public val <T> ProvidableCompositionLocal<T?>.currentOrThrow: T
    @Composable
    get() = current ?: error("CompositionLocal is null")

@Composable
public fun CurrentScreen() {
    val navigator = LocalNavigator.currentOrThrow
    val currentScreen = navigator.lastItem

    navigator.saveableState("currentScreen") {
        currentScreen.Content()
    }
}

@Composable
public fun Navigator(
    screen: Screen,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = { true },
    content: NavigatorContent = { CurrentScreen() }
) {
    Navigator(
        screens = listOf(screen),
        disposeBehavior = disposeBehavior,
        onBackPressed = onBackPressed,
        content = content
    )
}

@Composable
public fun Navigator(
    screens: List<Screen>,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = { true },
    content: NavigatorContent = { CurrentScreen() }
) {
    require(screens.isNotEmpty()) { "Navigator must have at least one screen" }

    CompositionLocalProvider(
        LocalNavigatorStateHolder providesDefault rememberSaveableStateHolder()
    ) {
        val navigator = rememberNavigator(screens, disposeBehavior, LocalNavigator.current)
        val lifecycleOwner = rememberScreenLifecycleOwner(navigator.lastItem)
        val hooks = lifecycleOwner.getHooks()

        if (navigator.parent?.disposeBehavior?.disposeNestedNavigators != false) {
            NavigatorDisposableEffect(navigator)
        }

        CompositionLocalProvider(
            LocalNavigator provides navigator,
            *hooks.providers.toTypedArray()
        ) {
            if (disposeBehavior.disposeSteps) {
                StepDisposableEffect(navigator)
            }

            NavigatorBackHandler(navigator, onBackPressed)

            content(navigator)
        }
    }
}

public class Navigator internal constructor(
    screens: List<Screen>,
    private val stateHolder: SaveableStateHolder,
    public val disposeBehavior: NavigatorDisposeBehavior,
    public val parent: Navigator? = null
) : Stack<Screen> by screens.toMutableStateStack(minSize = 1) {

    public val level: Int =
        parent?.level?.inc() ?: 0

    public val lastItem: Screen by derivedStateOf {
        lastItemOrNull ?: error("Navigator has no screen")
    }

    private val stateKeys = ThreadSafeList<String>()

    @Deprecated(
        message = "Use 'lastItem' instead. Will be removed in 1.0.0.",
        replaceWith = ReplaceWith("lastItem")
    )
    public val last: Screen by derivedStateOf {
        lastItem
    }

    @Composable
    public fun saveableState(
        key: String,
        screen: Screen = lastItem,
        content: @Composable () -> Unit
    ) {
        val stateKey = "${screen.key}:$key"
        stateKeys += stateKey
        stateHolder.SaveableStateProvider(stateKey, content)
    }

    public fun popUntilRoot() {
        popUntilRoot(this)
    }

    private tailrec fun popUntilRoot(navigator: Navigator) {
        navigator.popAll()

        if (navigator.parent != null) {
            popUntilRoot(navigator.parent)
        }
    }

    internal fun dispose(
        screen: Screen
    ) {
        ScreenModelStore.remove(screen)
        //LocalBlocStore.current.remove(screen)
        BlocStore.remove(screen)
        ScreenLifecycleStore.remove(screen)
        stateKeys
            .asSequence()
            .filter { it.startsWith(screen.key) }
            .forEach { key ->
                stateHolder.removeState(key)
                stateKeys -= key
            }
    }
}

public data class NavigatorDisposeBehavior(
    val disposeNestedNavigators: Boolean = true,
    val disposeSteps: Boolean = true,
)
