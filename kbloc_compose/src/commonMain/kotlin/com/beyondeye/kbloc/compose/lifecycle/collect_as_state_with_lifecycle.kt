package com.beyondeye.kbloc.compose.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

//see https://medium.com/androiddevelopers/consuming-flows-safely-in-jetpack-compose-cde014d0d5a3
@Composable
public expect fun <T : R, R> Flow<T>.mp_collectAsStateWithLifecycle(
    initial: R,
    context: CoroutineContext = EmptyCoroutineContext
): State<R>

//todo: mp_collectAsStateWithLifecycle help stopping collection of flows, not flows itself
//      for ways of making flow themselves lifecycle aware, see the end of the article
//      https://medium.com/androiddevelopers/consuming-flows-safely-in-jetpack-compose-cde014d0d5a3

