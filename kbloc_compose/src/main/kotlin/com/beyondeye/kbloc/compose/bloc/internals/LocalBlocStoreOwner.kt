package com.beyondeye.kbloc.compose.bloc.internals

/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf

/**
 * use this for for compose desktop
 * TODO put in inside top root composable (Window {} ?) for compose desktop
 */
val globalBlocStoreOwner=object :BlocStoreOwner {
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
