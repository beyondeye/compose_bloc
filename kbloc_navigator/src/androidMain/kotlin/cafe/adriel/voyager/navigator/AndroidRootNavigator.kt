package cafe.adriel.voyager.navigator

import androidx.compose.runtime.Composable
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cafe.adriel.voyager.core.model.ScreenModelStore
import cafe.adriel.voyager.core.model.internal.LocalScreenModelStoreOwner
import cafe.adriel.voyager.core.model.internal.ScreenModelStoreOwner
import cafe.adriel.voyager.core.screen.Screen
import com.beyondeye.kbloc.compose.bloc.internals.BlocStore
import com.beyondeye.kbloc.compose.bloc.internals.BlocStoreOwner
import com.beyondeye.kbloc.compose.bloc.internals.LocalBlocStoreOwner
import kotlinx.coroutines.runBlocking

internal class ActivityScreenModelStoreViewModel: ScreenModelStoreOwner,ViewModel() {
    override val screenModelStore: ScreenModelStore
        get() = store
    private val store:ScreenModelStore= ScreenModelStore()
    /**
     * This method will be called when this ViewModel is no longer used and will be destroyed.
     * <p>
     * It is useful when ViewModel observes some data and you need to clear this subscription to
     * prevent a leak of this ViewModel.
     * see implementation of [ScreenModelStore.remove]
     */
    override fun onCleared() {
        // first dispose all dependencies
        for (entry in store.dependencies) {
            val (instance,onDispose)= entry.value
            onDispose(instance)
        }
        // then dispose all screenModels
        for(entry in store.screenModels) {
            val model=entry.value
            model.onDispose()
        }
        super.onCleared()
    }
}

internal class ActivityBlocStoreViewModel: BlocStoreOwner,ViewModel() {
    override val blocStore: BlocStore
        get() = store
    private val store:BlocStore= BlocStore()
    /**
     * This method will be called when this ViewModel is no longer used and will be destroyed.
     * <p>
     * It is useful when ViewModel observes some data and you need to clear this subscription to
     * prevent a leak of this ViewModel.
     *
     * see implementation of [BlocStore.remove]
     */
    override fun onCleared() {
        //first clear depedendencies
        for (entry in store.blocs_dependencies) {
            val (instance,onDispose) = entry.value
            onDispose(instance)
        }
        //then clear blocs
        for (entry in store.blocs.entries) {
            val b=entry.value
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
    val activityScreenModelStoreOwner = ViewModelProvider(this).get(
        ActivityScreenModelStoreViewModel::class.java)
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
