package com.beyondeye.kbloc.compose.model.internals

import com.beyondeye.kbloc.compose.model.ScreenModelStore

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
interface ScreenModelStoreOwner {
    /**
     * Returns owned [ScreenModelStore]
     *
     * @return a `ScreenModelStore`
     */
    val screenModelStore: ScreenModelStore
}