package com.beyondeye.kbloc.compose.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

@Composable
public actual fun <T : R, R> Flow<T>.mp_collectAsStateWithLifecycle(
    initial: R,
    context: CoroutineContext
): State<R> = collectAsStateWithLifecycle(initialValue = initial,context=context)