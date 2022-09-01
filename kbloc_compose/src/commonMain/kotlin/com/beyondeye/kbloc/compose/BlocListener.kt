package com.beyondeye.kbloc.compose

import androidx.compose.runtime.*
import com.beyondeye.kbloc.compose.lifecycle.mp_collectAsStateWithLifecycle
import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.BlocBase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

/**
 * Signature for the `listenWhen` function which takes the previous `state`
 * and the current `state` and is responsible for returning a [Boolean] which
 * determines whether or not to call the registered listener of [BlocListener]
 * with the current `state`.
 * todo [BlocBuilderCondition] and [BlocListenerCondition] are exactly the same: merge them
 */
public typealias BlocListenerCondition<S> = @DisallowComposableCalls (previousState: S?, currentState: S) -> Boolean


/**
 * todo [listenWhenFilter] and [buildWhenFilter] are exaclty the same: merge them
 * @param listenWhen: see [BlocListenerCondition]
 */
@PublishedApi
internal fun <BlocAState> listenWhenFilter(
    srcFlow: Flow<BlocAState>,
    listenWhen: BlocListenerCondition<BlocAState>
): Flow<BlocAState> {
    var prevState: BlocAState? = null
    return srcFlow.transform { curState ->
        if (listenWhen(prevState, curState)) {
            emit(curState)
        }
        prevState = curState
    }
}

/**
 * [BlocListener] handles retrieving a bloc of the specified type from the registered blocs in the
 * current composable subtree (see [BlocProvider]) and start listening to the associated stream of
 * bloc state updates. Invoke [listener] callback in response to `state` changes in the [Bloc].
 * Note that [listener] is not a Composable. It is  a (potentially) suspendable method,
 * that is invoked with [LaunchedEffect]
 *
 * The [listener] is guaranteed to only be called once for each `state` change
 * unlike the `content` method in [BlocBuilder], that can be triggered because of recomposition
 * of the current composable tree
 *
 * An optional [blocTag] parameter can be specified in order to identify a specific
 * bloc instance in the case where there is more than one instance of a bloc of the same type
 * registered for the current composable subtree (see [BlocProvider])
 * The [blocTag] parameter is not present in the original flutter_bloc implementation *
 *
 * An optional [listenWhen] parameter can be provided for more granular control
 * over when [listener] is called.
 * [listenWhen] takes the previous `state` and current `state` and must
 * return a [Boolean] which determines whether or not the [listener] function
 * will be invoked.
 * For the first call to [listenWhen] the previous `state` will be initialized to the `state` of the [Bloc]
 * when the [BlocListener] was initialized.
 * NOTE: in original DART code BlocListener is a Widget that declare a child.
 * In Compose there is no need to do such a thing. By removing the child argument
 * Also there is actually no need for the MultiBlocListener class that is present in flutter_bloc code
 * NOTE that if [listenWhen] condition change after initial composition the change
 * will not be taken into account
 */
@Composable
public inline fun <reified BlocA : BlocBase<BlocAState>, BlocAState : Any> BlocListener(
    blocTag: String? = null,
    noinline listenWhen: BlocListenerCondition<BlocAState>? = null,
    crossinline listener: @DisallowComposableCalls suspend (BlocAState) -> Unit
) {
    rememberProvidedBlocOf<BlocA>(blocTag)?.let { b ->
        BlocListenerCore(b, listenWhen, listener)
    }
}


/**
 * same as previous method but with explicitely specified bloc instance [externallyProvidedBlock]
 * not retrieved implicitely from current registered blocs in the current composable subtree
 * see [BlocProvider]
 * Use this method if for example you have retrieved the Bloc already with [rememberProvidedBlocOf]
 */
@Composable
public inline fun <reified BlocA : BlocBase<BlocAState>, BlocAState : Any> BlocListener(
    externallyProvidedBlock: BlocA,
    noinline listenWhen: BlocListenerCondition<BlocAState>? = null,
    crossinline listener: @DisallowComposableCalls suspend (BlocAState) -> Unit
) {
    val b = remember { externallyProvidedBlock }
    BlocListenerCore(b, listenWhen, listener)
}

@PublishedApi
@Composable
internal inline fun <reified BlocA : BlocBase<BlocAState>, BlocAState : Any> BlocListenerCore(
    b: BlocA,
    noinline listenWhen: BlocListenerCondition<BlocAState>?,
    crossinline listener: @DisallowComposableCalls suspend (BlocAState) -> Unit,
) {
    val collect_scope = rememberCoroutineScope()
    val stream = remember {
        if (listenWhen == null) b.stream else {
            listenWhenFilter(b.stream, listenWhen)
        }
    }
    val filtered_start_state= remember {
        val start_state=b.state
        if(listenWhen==null) start_state else if (listenWhen(null,start_state)) start_state else null
    }

    //collection automatically paused when activity paused
    val state: BlocAState? by stream.mp_collectAsStateWithLifecycle(filtered_start_state, collect_scope.coroutineContext)
    //TODO according to the documentation of LaunchedEffect, what I am doing here, if I understand
    // the docs correclty, that is o (re-)launch ongoing tasks in response to callback
    // * events by way of storing callback data in [MutableState] passed to [key]
    // is something that should no be done: need to understand better
    LaunchedEffect(state) {
        if(state!=null) listener(state!!) //state can be null if initial state does not satisfy listenWhen condition
    }
}


