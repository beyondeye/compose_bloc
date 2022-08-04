package com.beyondeye.kbloc.compose.android.screen

import androidx.compose.runtime.Composable
import com.beyondeye.kbloc.compose.navigator.*
import com.beyondeye.kbloc.compose.screen.Screen
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.beyondeye.kbloc.compose.bloc.internals.BlocStore
import com.beyondeye.kbloc.compose.bloc.internals.BlocStoreOwner
import com.beyondeye.kbloc.compose.bloc.internals.LocalBlocStoreOwner
import com.beyondeye.kbloc.compose.model.internals.LocalScreenModelStoreOwner
import com.beyondeye.kbloc.compose.model.ScreenModelStore
import com.beyondeye.kbloc.compose.model.coroutineScope
import com.beyondeye.kbloc.compose.model.coroutineScopeOrNull
import com.beyondeye.kbloc.compose.model.internals.ScreenModelStoreOwner
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking

private class ActivityScreenModelStoreViewModel: ScreenModelStoreOwner,ViewModel() {
    override val screenModelStore: ScreenModelStore
        get() = store
    private val store:ScreenModelStore= ScreenModelStore()
    /**
     * This method will be called when this ViewModel is no longer used and will be destroyed.
     * <p>
     * It is useful when ViewModel observes some data and you need to clear this subscription to
     * prevent a leak of this ViewModel.
     */
    override fun onCleared() {
        for(entry in store.screenModels) {
            val model=entry.value
            /**
             * TODO: implementation here is not complete, check again implementation of [ScreenModelStore.remove]
             * it is also problematic to have two different places with the same logic but written differently, to dispose data
             */
            aa
            model.onDispose()
            store.coroutineScopeOrNull(model)?.cancel()
        }
        super.onCleared()
    }
}

private class ActivityBlocStoreViewModel: BlocStoreOwner,ViewModel() {
    override val blocStore: BlocStore
        get() = store
    private val store:BlocStore= BlocStore()
    /**
     * This method will be called when this ViewModel is no longer used and will be destroyed.
     * <p>
     * It is useful when ViewModel observes some data and you need to clear this subscription to
     * prevent a leak of this ViewModel.
     */
    override fun onCleared() {
        for (entry in store.blocs.entries) {
            val b=entry.value
            /**
             * TODO: implementation here is not complete, check again implementation of [BlocStore.remove]
             * it is also problematic to have two different places with the same logic but written differently, to dispose data
             */
            aa
            runBlocking {
                b.dispose()
            }
        }
        super.onCleared()
    }
}
/**
 * *DARIO*
 *  * In order to correctly handle lifecycle of screen models and blocs associated to some activity,
 * we need some special initialization to be done at the root navigator for the activity
 */
@Composable
public fun ComponentActivity.RootNavigator(screen: Screen,
                                  disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
                                  onBackPressed: OnBackPressed = { true },
                                  content: NavigatorContent = { CurrentScreen() }
) {
        RootNavigator(
            screens = listOf(screen),
            disposeBehavior = disposeBehavior,
            onBackPressed = onBackPressed,
            content = content
        )
    }


/**
 * *DARIO*
 * In order to correctly handle lifecycle of screen models and blocs associated to some activity,
 * we need some special initialization to be done at the root navigator for the activity
 */
@Composable
public fun ComponentActivity.RootNavigator(
    screens: List<Screen>,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    onBackPressed: OnBackPressed = { true },
    content: NavigatorContent = { CurrentScreen() }
) {
    val activityScreenModelStoreOwner = ViewModelProvider(this).get(ActivityScreenModelStoreViewModel::class.java)
    val activityBlocStoreOwner = ViewModelProvider(this).get(ActivityBlocStoreViewModel::class.java)
    //TODO store directly ScreenModelStore and not ScreenModelStoreOwner
    //TODO store directly BlocStore and not BlocStoreOwner
    CompositionLocalProvider(
        LocalScreenModelStoreOwner.provides(activityScreenModelStoreOwner),
        LocalBlocStoreOwner.provides(activityBlocStoreOwner))
    {
        Navigator(
            screens = screens,
            disposeBehavior = disposeBehavior,
            onBackPressed = onBackPressed,
            content = content
        )
    }
}
