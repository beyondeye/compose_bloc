package com.beyondeye.kbloc.compose.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import com.beyondeye.kbloc.compose.navigator.Navigator
import com.beyondeye.kbloc.compose.screen.Screen
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * coroutineScope is defined as dependency of a [ScreenModel], so that when a ScreenModel is disposed
 * also its dependencies are disposed. Note that by default, this scope start coroutines in the main thread
 * you can change the dispatcher for example by supplying a dispatcher argument to [CoroutineScope.async] and [CoroutineScope.launch]
 * or creating a new [CoroutineScope] with [CoroutineScope.newCoroutineContext]
 */
public val ScreenModel.coroutineScope: CoroutineScope
    get() = ScreenModelStore.getOrPutDependency(
        screenModel = this,
        name = "ScreenModelCoroutineScope",
        factory = { key -> MainScope() + CoroutineName(key) }, //MainScope() means Dispatchers.Main, the main thread, CoroutineName is only for debygging
        onDispose = { scope -> scope.cancel() } //cancel all coroutine in this scope when onDispose is called
    )

/**
 * method usually called at the beginning of  [Screen.Content] method, in order to obtain an instance
 * of [ScreenModel] of the specified type that will be associated to the [Screen].
 * The [ScreenModel.onDispose] method will be automatically called when the [Screen] will be popped
 * from the [Navigator] stack
 */
@Composable
public inline fun <reified T : ScreenModel> Screen.rememberScreenModel(
    tag: String? = null,
    factory: @DisallowComposableCalls () -> T
): T =
    remember(ScreenModelStore.getKey<T>(this, tag)) {
        ScreenModelStore.getOrPut(this, tag, factory)
    }

/**
 * a [ScreenModel] is data associated to specific [Screen] that is created or injected
 * when method [Screen.rememberScreenModel] is called, usually at the beginning of the definition
 * of implementation of [Screen.Content].
 * The [onDispose] method is automatically called when
 * the screen is popped from the [Navigator] stack
 * Note that there can be more than one [ScreenModel] for the same [Screen] and even more than one
 * [ScreenModel] of the same type (in such case a user defined tag must be specified in [rememberScreenModel]
 * for each instance.
 */
public interface ScreenModel {

    public fun onDispose() {}
}

public abstract class StateScreenModel<S>(initialState: S) : ScreenModel {

    protected val mutableState: MutableStateFlow<S> = MutableStateFlow(initialState)
    public val state: StateFlow<S> = mutableState.asStateFlow()
}