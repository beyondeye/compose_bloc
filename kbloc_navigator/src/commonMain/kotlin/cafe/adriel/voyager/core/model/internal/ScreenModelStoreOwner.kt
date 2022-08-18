package cafe.adriel.voyager.core.model.internal

import cafe.adriel.voyager.core.model.ScreenModelStore


/**
 * *DARIO*
 * A scope that owns [ScreenModelStore].
 *
 *
 * A responsibility of an implementation of this interface is to retain owned ScreenModelStore
 * during the configuration changes and call [ScreenModelStore.clear], when this scope is
 * going to be destroyed.
 *
 */
public interface ScreenModelStoreOwner {
    /**
     * Returns owned [ScreenModelStore]
     *
     * @return a `ScreenModelStore`
     */
    public val screenModelStore: ScreenModelStore
}