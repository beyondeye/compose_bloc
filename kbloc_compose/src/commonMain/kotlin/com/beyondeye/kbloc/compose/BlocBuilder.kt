package com.beyondeye.kbloc.compose

import androidx.compose.runtime.*
import com.beyondeye.kbloc.compose.lifecycle.mp_collectAsStateWithLifecycle
import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.BlocBase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform


/**
* Signature for the `buildWhen` function which takes the previous `state` and
* the current `state` and is responsible for returning a [Boolean] which
* determines whether or not to trigger a rebuild of [BlocBuilder] with the current `state`.
 */
public typealias BlocBuilderCondition<S> = @DisallowComposableCalls (previous:S?,current:S)->Boolean

/**
 *  create a new flow that filter the [srcFlow] and emit only states that satisfy the [buildWhen]
 *  condition, that compare prevState and curState
 */
@PublishedApi
internal fun <BlocAState>buildWhenFilter(srcFlow:Flow<BlocAState>, buildWhen: BlocBuilderCondition<BlocAState>): Flow<BlocAState> {
    var prevState:BlocAState?=null
    return  srcFlow.transform { curState->
        if(buildWhen(prevState,curState)) {
            emit(curState)
        }
        prevState=curState
    }
}

/**
 * [BlocBuilder] handles retrieving a bloc of the specified type from the registered blocs in the
 * current composable subtree (see [BlocProvider]) and start listening to the associated stream of
 * bloc state updates, passing it to the [content] composable method.
 * Each new value emitted in the bloc stream will trigger recomposition.
 *
 *  Please refer to [BlocListener] if you instead want to "do" anything in response to
 * `state` changes such as navigation, showing a dialog, etc...
 *
 * An optional [blocTag] parameter can be specified in order to identify a specific
 * bloc instance in case there is more than one instance of a bloc of same type
 * registered for the current composable subtree (see [BlocProvider])
 * [blocTag] parameter is not present in the original flutter_bloc implementation
 *
 * An optional [buildWhen] parameter can be provided for more granular control over
 * what specific kind of state change should trigger recomposition
 * [buildWhen] will be invoked on each [Bloc] `state` change.
 * [buildWhen] takes the previous `state` and current `state` and must
 * return a [Boolean] which determines whether or not the [content] composable function will
 * be triggered with the new state
 * For the first call to [buildWhen], the previous `state` will be initialized to the `state` of the [Bloc] when
 * the [BlocBuilder] was initialized.
 * NOTE that if [buildWhen] condition change after initial composition the change
 * will not be taken into account
 */
@Composable
public inline fun <reified BlocA:BlocBase<BlocAState>,BlocAState:Any> BlocBuilder(
    blocTag:String?=null,
    noinline buildWhen: BlocBuilderCondition<BlocAState>?=null,
    content:@Composable (BlocAState)->Unit)
{
    rememberProvidedBlocOf<BlocA>(blocTag)?.let { b->
        BlockBuilderCore(b, buildWhen, content)
    }

}

/**
 * same as previous method but with explicitely specified bloc instance [externallyProvidedBlock]
 * not retrieved implicitely from current registered blocs in the current composable subtree
 * see [BlocProvider]
 * Use this method if for example you have retrieved the Bloc already with [rememberProvidedBlocOf]
 */
@Composable
public inline fun <reified BlocA:BlocBase<BlocAState>,BlocAState:Any> BlocBuilder(
    externallyProvidedBlock:BlocA,
    noinline buildWhen: BlocBuilderCondition<BlocAState>?=null,
    content:@Composable (BlocAState)->Unit)
{
    val b =  remember { externallyProvidedBlock }
    BlockBuilderCore(b,buildWhen,content)
}


@Composable
@PublishedApi
internal inline fun <reified BlocA : BlocBase<BlocAState>, BlocAState : Any> BlockBuilderCore(
    b: BlocA,
    noinline buildWhen: BlocBuilderCondition<BlocAState>?,
    content: @Composable (BlocAState) -> Unit
) {
    val collect_scope = rememberCoroutineScope()
    val stream = remember {
        if (buildWhen == null) b.stream else {
            buildWhenFilter(b.stream, buildWhen)
        }
    }
    val filtered_start_state = remember {
        val start_state=b.state
        if(buildWhen==null) start_state else if (buildWhen(null,start_state)) start_state else null
    }

    //collection automatically paused when activity paused
    val state: BlocAState? by stream.mp_collectAsStateWithLifecycle(filtered_start_state, collect_scope.coroutineContext)
    if(state!=null) content(state!!) //state can be null if initial state does not satisfy buildWhen condition
}
