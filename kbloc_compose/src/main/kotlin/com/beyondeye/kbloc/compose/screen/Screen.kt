package com.beyondeye.kbloc.compose.screen

import androidx.compose.runtime.Composable

public interface Screen {

    public open val key: ScreenKey

    @Composable
    public fun Content()
}

internal fun Screen.commonKeyGeneration() =
    this::class.qualifiedName ?: error("Default ScreenKey not found, please provide your own key")
