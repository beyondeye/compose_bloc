package com.beyondeye.kbloc.compose.internal

/**
 * *DARIO*
 * A scope that owns [BlocStore].
 *
 *
 * A responsibility of an implementation of this interface is to retain owned [BlocStore]
 * during the configuration changes and call [BlocStore.clear], when this scope is
 * going to be destroyed.
 *
 */
@PublishedApi
internal interface BlocStoreOwner {
    /**
     * Returns owned [BlocStore]
     *
     * @return a `ScreenModelStore`
     */
    val blocStore: BlocStore
}