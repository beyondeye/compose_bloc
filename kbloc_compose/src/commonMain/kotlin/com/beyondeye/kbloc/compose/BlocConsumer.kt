package com.beyondeye.kbloc.compose

import androidx.compose.runtime.*
import com.beyondeye.kbloc.compose.lifecycle.mp_collectAsStateWithLifecycle
import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.BlocBase

/**
 * [BlocConsumer] exposes a [content] and [listener] in order react to new
 * states.
 * [BlocConsumer] is equivalent to a combined [BlocBuilder] and [BlocListener]
 * [BlocConsumer] should only be used when it is necessary to both rebuild UI
 * and execute other reactions to state changes in the [Bloc].
 */
@Composable
public inline fun <reified BlocA: BlocBase<BlocAState>,BlocAState:Any> BlocConsumer(
    blocTag:String?=null,
    noinline buildWhen: BlocBuilderCondition<BlocAState>?=null,
    noinline listenWhen: BlocListenerCondition<BlocAState>?=null,
    crossinline listener: @DisallowComposableCalls suspend (BlocAState) -> Unit,
    crossinline content:@Composable (BlocAState)->Unit)
{
    rememberProvidedBlocOf<BlocA>(blocTag)?.let { b->
        BlocConsumerCore(b, listenWhen, listener, buildWhen, content)
    }
}

@Composable
public inline fun <reified BlocA: BlocBase<BlocAState>,BlocAState:Any> BlocConsumer(
    externallyProvidedBlock:BlocA,
    noinline buildWhen: BlocBuilderCondition<BlocAState>?=null,
    noinline listenWhen: BlocListenerCondition<BlocAState>?=null,
    crossinline listener: @DisallowComposableCalls suspend (BlocAState) -> Unit,
    crossinline content:@Composable (BlocAState)->Unit)
{
    val b =  remember { externallyProvidedBlock }
    BlocConsumerCore(b, listenWhen, listener, buildWhen, content)
}

@Composable
@PublishedApi
internal inline fun <reified BlocA : BlocBase<BlocAState>, BlocAState : Any> BlocConsumerCore(
    b: BlocA,
    noinline listenWhen: BlocListenerCondition<BlocAState>?,
    crossinline listener: @DisallowComposableCalls suspend (BlocAState) -> Unit,
    noinline buildWhen: BlocBuilderCondition<BlocAState>?,
    crossinline content: @Composable (BlocAState) -> Unit
) {
    val collect_scope = rememberCoroutineScope()
    val listen_stream = remember {
        if (listenWhen == null) b.stream else listenWhenFilter(b.stream, listenWhen)
    }
    val filtered_start_state_listen=remember {
        val start_state=b.state
        if(listenWhen==null) start_state else if (listenWhen(null,start_state)) start_state else null
    }

    //collection automatically paused when activity paused
    val listen_state: BlocAState? by listen_stream.mp_collectAsStateWithLifecycle(
        filtered_start_state_listen,
        collect_scope.coroutineContext
    )
    //TODO according to the documentation of LaunchedEffect, what I am doing here, if I understand
    // the docs correclty, that is o (re-)launch ongoing tasks in response to callback
    // * events by way of storing callback data in [MutableState] passed to [key]
    // is something that should no be done: need to understand better
    LaunchedEffect(listen_state) {
        if(listen_state!=null) listener(listen_state!!) //listen_state can be null if initial state does not satisfy listenWhen condition
    }
    val build_stream = remember {
        if (buildWhen == null) b.stream else
            buildWhenFilter(b.stream, buildWhen)
    }

    val filtered_start_state_build= remember {
        val start_state=b.state
        if(buildWhen==null) start_state else if (buildWhen(null,start_state)) start_state else null
    }

    //collection automatically paused when activity paused
    val build_state: BlocAState? by build_stream.mp_collectAsStateWithLifecycle(
        filtered_start_state_build,
        collect_scope.coroutineContext
    )
    if(build_state!=null) content(build_state!!) //build_state can be null if initial state does not satisfy buildWhen condition
}
