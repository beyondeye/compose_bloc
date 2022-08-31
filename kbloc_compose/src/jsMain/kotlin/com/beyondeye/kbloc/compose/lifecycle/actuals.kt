package com.beyondeye.kbloc.compose.lifecycle

import androidx.compose.runtime.Composable

import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

/**
 * todo no lifecycle events to handle in js?
 */
@Composable
actual fun <T : R, R> Flow<T>.mp_collectAsStateWithLifecycle(
    initial: R,
    context: CoroutineContext
): State<R> = collectAsState(initial,context)
