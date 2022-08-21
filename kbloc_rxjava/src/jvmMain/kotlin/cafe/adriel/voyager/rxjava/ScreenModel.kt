package cafe.adriel.voyager.rxjava

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.ScreenModelStore
import cafe.adriel.voyager.core.model.internal.globalScreenModelStoreOwner
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

public val ScreenModel.disposables: CompositeDisposable
    //TODO don't use globalScreenModelStoreOwner
    //this is a *HACK* because implementation is not complete
    //I should instead refer to root ScreenModelStoreOwner: for an activity or for a window
    // or integrate with LocalScreenModelStoreOwner, but this would need making this get()
    // method composable, which changes the original voyager api
    get() = globalScreenModelStoreOwner.screenModelStore.getOrPutDependency(
        screenModel = this,
        name = "ScreenModelCompositeDisposable",
        factory = { CompositeDisposable() },
        onDispose = { disposables -> disposables.clear() }
    )

public abstract class RxScreenModel<S : Any> : ScreenModel {

    protected val mutableState: BehaviorSubject<S> = BehaviorSubject.create()
    public val state: Observable<S> = mutableState
}
