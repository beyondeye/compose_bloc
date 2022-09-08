package cafe.adriel.voyager.navigator

import androidx.compose.runtime.Composable
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cafe.adriel.voyager.core.model.ScreenModelStore
import cafe.adriel.voyager.core.model.internal.LocalScreenModelStoreOwner
import cafe.adriel.voyager.core.model.internal.ScreenModelStoreOwner
import cafe.adriel.voyager.core.screen.Screen
import com.beyondeye.kbloc.compose.internal.BlocStoreOwner
import com.beyondeye.kbloc.compose.internal.LocalBlocStoreOwner
import com.beyondeye.kbloc.compose.internal.BlocStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async


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
        //Log.e(LOGTAG,"ActivityBlocStoreViewModel:onCleared()")
        // first dispose all dependencies
        for (entry in store.dependencies.value) {
            val (instance,onDispose)= entry.value
            onDispose(instance)
        }
        // then dispose all screenModels
        for(entry in store.screenModels.value) {
            val model=entry.value
            //Log.e(LOGTAG,"Disposing screenmodel $model")
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
        //Log.e(LOGTAG,"ActivityBlocStoreViewModel:onCleared()")
        //first clear depedendencies
        for (entry in store.blocs_dependencies_value) {
            val (instance,onDispose) = entry.value
            onDispose(instance)
        }
        //then clear blocs
        for (entry in store.blocs_value.entries) {
            val b=entry.value
            //Log.e(LOGTAG,"Disposing bloc $b")
            GlobalScope.async {
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
    _init_kbloc_for_subtree_android(this) {
        Navigator(
            screens = screens,
            disposeBehavior = disposeBehavior,
            onBackPressed = onBackPressed,
            content = content
        )
    }
}

/**
 * DON'T CALL THIS METHOD DIRECTLY, IT IS USED UNDER THE HOOD when initializing the root navigator
 * [provided_values] argument is providing additional CompositionLocalProvider definitions to the subtree
 */
@Composable
public fun _init_kbloc_for_subtree_android(
    activity:ComponentActivity,
    vararg provided_values: ProvidedValue<*>,
    content:@Composable () ->Unit)
{
    //TODO store directly ScreenModelStore and not ScreenModelStoreOwner
    val activityScreenModelStoreOwner = ViewModelProvider(activity).get(
        ActivityScreenModelStoreViewModel::class.java)
    //TODO store directly BlocStore and not BlocStoreOwner
    val activityBlocStoreOwner = ViewModelProvider(activity).get(ActivityBlocStoreViewModel::class.java)
    CompositionLocalProvider(
        *provided_values,
        LocalScreenModelStoreOwner.provides(activityScreenModelStoreOwner),
        LocalBlocStoreOwner.provides(activityBlocStoreOwner))
    {
        content()
    }
}
