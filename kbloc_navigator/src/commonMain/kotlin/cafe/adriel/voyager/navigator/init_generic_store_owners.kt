package cafe.adriel.voyager.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidedValue
import cafe.adriel.voyager.core.model.ScreenModelStore
import cafe.adriel.voyager.core.model.internal.LocalScreenModelStoreOwner
import cafe.adriel.voyager.core.model.internal.ScreenModelStoreOwner
import com.beyondeye.kbloc.compose.internal.BlocStore
import com.beyondeye.kbloc.compose.internal.BlocStoreOwner
import com.beyondeye.kbloc.compose.internal.LocalBlocStoreOwner
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

//make this public to make it accessible to kbloc_router: not a very good idea: perhaps unify kbloc_router with kbloc_navigator
public class GenericScreenModelStoreOwner: ScreenModelStoreOwner {
    /**
     * This method will be called when this ElementScreenModelStoreOwner is no longer used and
     * will be destroyed.
     * see also implementation of [ScreenModelStore.remove]
     */
    public fun onCleared() {
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
//make this public to make it accessible to kbloc_router: not a very good idea: perhaps unify kbloc_router with kbloc_navigator
public class GenericBlocStoreOwner : BlocStoreOwner {
    override val blocStore: BlocStore
        get() = store
    private val store: BlocStore = BlocStore()

    /**
     * This method will be called when this ElementBlocStoreOwner is no longer used and will be destroyed.
     *
     * see also implementation of [BlocStore.remove]
     */
    public fun onCleared() {
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
 * this method is used in desktop and JS platform.
 * For android there is a custom implementation based on ViewModel
 * [provided_values] argument is providing additional CompositionLocalProvider definitions to the subtree
 */
@Composable
public fun init_kbloc_for_subtree(
    vararg provided_values: ProvidedValue<*>, content:@Composable () ->Unit) {
    val screenModelStore= GenericScreenModelStoreOwner()
    val blocStore= GenericBlocStoreOwner()
    CompositionLocalProvider(
        *provided_values,
        LocalScreenModelStoreOwner.provides(screenModelStore),
        LocalBlocStoreOwner.provides(blocStore))
    {
        content()
        //TODO check onDispose() is actually  triggered and when
        DisposableEffect(true) {
            onDispose {
                //Napier.d("onCleared for screenModelStore  and blocStore")
                screenModelStore.onCleared()
                blocStore.onCleared()
            }
        }
    }
}