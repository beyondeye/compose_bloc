package cafe.adriel.voyager.navigator

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.internal.NavigatorBackHandler
import com.beyondeye.kbloc.compose.internal.BlocStore
import cafe.adriel.voyager.core.model.ScreenModelStore
import cafe.adriel.voyager.core.stack.SnapshotStateStack
import cafe.adriel.voyager.navigator.internal.LocalNavigatorStateHolder
import cafe.adriel.voyager.navigator.internal.rememberNavigator
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.persistentListOf
import kotlin.js.JsName


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
        //*DARIO* Screen is no more LifecyleOwner, it was not working
        // see for example https://github.com/adrielcafe/voyager/issues/62
        //  lifecycleOwner must remain the parent activity of the screen
        /*
        val lifecycleOwner = rememberScreenLifecycleOwner(navigator.lastItem)
        val hooks = lifecycleOwner.getHooks()
        val compositionLocalOverrides=hooks.providers.toTypedArray()
         */
        val compositionLocalOverrides = arrayOf<ProvidedValue<*>>()

        /* *DARIO* this was not working, see https://github.com/adrielcafe/voyager/issues/62
         * we have substituted this we explicit call to navigator.dispose()
         * TODO what to do when there are screens to dispose under this screen (child screen)
         *  or is it impossible now that we dispose screen on explicitely when screen is popped?
         */
        /*
        if (navigator.parent?.disposeBehavior?.disposeNestedNavigators != false) {
            NavigatorDisposableEffect(navigator)
        }
         */

        CompositionLocalProvider(
            LocalNavigator provides navigator,
            *compositionLocalOverrides
        ) {
            /* *DARIO* this was not working see https://github.com/adrielcafe/voyager/issues/62
            * TODO: *SMS* I am not sure with what this was used for and I am not sure with what I should
            *  substitute it
            */
            /*
            if (disposeBehavior.disposeSteps) {
                StepDisposableEffect(navigator)
            }
             */

            NavigatorBackHandler(navigator, onBackPressed)

            content(navigator)
        }
    }
}

public class Navigator internal constructor(
    screens: List<Screen>,
    private val stateHolder: SaveableStateHolder,
    public val disposeBehavior: NavigatorDisposeBehavior,
    public val parent: Navigator? = null,
    internal val screenModelStore: ScreenModelStore,
    internal val blocStore: BlocStore,
) : SnapshotStateStack<Screen>(screens,1) {

    public val level: Int =
        parent?.level?.inc() ?: 0

    public val lastItem: Screen by derivedStateOf {
        lastItemOrNull ?: error("Navigator has no screen")
    }

    // a reference to a persistent list whose reference is atomically modified, instead of a ThreadSafeList
    //atomicref data should not be public according to atomicfu docs
    private val stateKeys = atomic(persistentListOf<String>())

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
        stateKeys.update { it.add(stateKey) }
        stateHolder.SaveableStateProvider(stateKey, content)
    }

    @JsName("popUntilRoot_this")
    public fun popUntilRoot() {
        popUntilRoot(this)
    }


    override fun pop(): Boolean {
        val last= pop_and_return_last()
        last?.let { dispose(it)  }
        return last!=null
    }

    override fun popAll() {
        popUntil({ false })
    }

    override fun popUntil(predicate: (Screen) -> Boolean): Boolean {
        val popped=popUntil_and_return_popped(predicate)
        for (p in popped) { dispose(p) }
        return popped.size>0
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
        screenModelStore.remove(screen)
        blocStore.remove(screen)
        //*DARIO* we have currently disabled all lifecycle handling linked to specific screens not activity
        //ScreenLifecycleStore.remove(screen)
        stateKeys.value
            .asSequence()
            .filter { it.startsWith(screen.key) }
            .forEach { key ->
                stateHolder.removeState(key)
                stateKeys.update { it.minus(key) }
            }
    }
}

public data class NavigatorDisposeBehavior(
    val disposeNestedNavigators: Boolean = true,
    val disposeSteps: Boolean = true,
)
