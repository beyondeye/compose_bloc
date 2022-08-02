package com.beyondeye.kbloc.compose.bloc

import androidx.compose.runtime.*
import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.BlocBase

/**
 * {@template bloc_consumer}
 * [BlocConsumer] exposes a [content] and [listener] in order react to new
 * states.
 * [BlocConsumer] is equivalent to a combined [BlocBuilder] and [BlocListener]
 * [BlocConsumer] should only be used when it is necessary to both rebuild UI
 * and execute other reactions to state changes in the [Bloc].
 */
@Composable
public inline fun <reified BlocA: BlocBase<BlocAState>,BlocAState:Any> BlocConsumer(
    blocTag:String?=null,
    noinline buildWhen:BlocBuilderCondition<BlocAState>?=null,
    noinline listenWhen: BlocListenerCondition<BlocAState>?=null,
    crossinline listener: @DisallowComposableCalls suspend (BlocAState) -> Unit,
    crossinline content:@Composable (BlocAState)->Unit)
{
    rememberProvidedBlocOf<BlocA>(blocTag) ?.let { b->
        BlocConsumerCore(b, listenWhen, listener, buildWhen, content)
    }
}

@Composable
public inline fun <reified BlocA: BlocBase<BlocAState>,BlocAState:Any> BlocConsumer(
    externallyProvidedBlock:BlocA,
    noinline buildWhen:BlocBuilderCondition<BlocAState>?=null,
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
    val listen_stream = if (listenWhen == null) b.stream else {
        listenWhenFilter(b.stream, listenWhen)
    }
    val listen_state: BlocAState by listen_stream.collectAsState(
        b.state,
        collect_scope.coroutineContext
    )
    //TODO according to the documentation of LaunchedEffect, what I am doing here, if I understand
    // the docs correclty, that is o (re-)launch ongoing tasks in response to callback
    // * events by way of storing callback data in [MutableState] passed to [key]
    // is something that should no be done: need to understand better
    LaunchedEffect(listen_state) {
        listener(listen_state)
    }
    val build_stream = if (buildWhen == null) b.stream else {
        buildWhenFilter(b.stream, buildWhen)
    }
    val build_state: BlocAState by build_stream.collectAsState(
        b.state,
        collect_scope.coroutineContext
    )
    content(build_state)
}
