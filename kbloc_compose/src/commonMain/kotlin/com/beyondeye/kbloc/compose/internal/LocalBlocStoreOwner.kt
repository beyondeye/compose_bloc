package com.beyondeye.kbloc.compose.internal


import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf

/**
 * use this for for compose desktop
 * TODO put in inside top root composable (Window {} ?) for compose desktop
 */
internal val globalBlocStoreOwner:BlocStoreOwner=object :BlocStoreOwner {
    private val _store=BlocStore()
    override val blocStore: BlocStore
        get() = _store
}
/**
 * *DARIO*
 * The CompositionLocal containing the current [BlocStoreOwner].
 */
public object LocalBlocStoreOwner {
    private val LocalBlocStoreOwner =
        compositionLocalOf<BlocStoreOwner?> { null }

    /**
     * Returns current composition local value for the owner or `null` if one has not
     * been provided
     */
    public val current: BlocStoreOwner
        @Composable
        get() = LocalBlocStoreOwner.current
            ?: throw Exception("It seem you are missing definition of a root navigator!")


    /**
     * Associates a [LocalBlocStoreOwner] key to a value in a call to
     * [CompositionLocalProvider].
     */
    public infix fun provides(blocStoreOwner: BlocStoreOwner):
            ProvidedValue<BlocStoreOwner?> {
        return LocalBlocStoreOwner.provides(blocStoreOwner)
    }
}
