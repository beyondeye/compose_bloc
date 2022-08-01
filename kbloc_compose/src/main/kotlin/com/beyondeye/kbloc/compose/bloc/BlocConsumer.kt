package com.beyondeye.kbloc.compose.bloc

import androidx.compose.runtime.*
import com.beyondeye.kbloc.compose.bloc.internals.rememberBloc
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.core.BlocBase
import kotlinx.coroutines.CoroutineScope

/**
 * {@template bloc_consumer}
 * [BlocConsumer] exposes a [builder] and [listener] in order react to new
 * states.
 * [BlocConsumer] is analogous to a nested `BlocListener`
 * and `BlocBuilder` but reduces the amount of boilerplate needed.
 * [BlocConsumer] should only be used when it is necessary to both rebuild UI
 * and execute other reactions to state changes in the [bloc].
 *
 * [BlocConsumer] takes a required `BlocWidgetBuilder`
 * and `BlocWidgetListener` and an optional [bloc],
 * `BlocBuilderCondition`, and `BlocListenerCondition`.
 *
 * If the [bloc] parameter is omitted, [BlocConsumer] will automatically
 * perform a lookup using `BlocProvider` and the current `BuildContext`.
 *
 * ```dart
 * BlocConsumer<BlocA, BlocAState>(
 *   listener: (context, state) {
 *     // do stuff here based on BlocA's state
 *   },
 *   builder: (context, state) {
 *     // return widget here based on BlocA's state
 *   }
 * )
 * ```
 *
 * An optional [listenWhen] and [buildWhen] can be implemented for more
 * granular control over when [listener] and [builder] are called.
 * The [listenWhen] and [buildWhen] will be invoked on each [bloc] `state`
 * change.
 * They each take the previous `state` and current `state` and must return
 * a [bool] which determines whether or not the [builder] and/or [listener]
 * function will be invoked.
 * The previous `state` will be initialized to the `state` of the [bloc] when
 * the [BlocConsumer] is initialized.
 * [listenWhen] and [buildWhen] are optional and if they aren't implemented,
 * they will default to `true`.
 *
 * ```dart
 * BlocConsumer<BlocA, BlocAState>(
 *   listenWhen: (previous, current) {
 *     // return true/false to determine whether or not
 *     // to invoke listener with state
 *   },
 *   listener: (context, state) {
 *     // do stuff here based on BlocA's state
 *   },
 *   buildWhen: (previous, current) {
 *     // return true/false to determine whether or not
 *     // to rebuild the widget with state
 *   },
 *   builder: (context, state) {
 *     // return widget here based on BlocA's state
 *   }
 * )
 * ```
 * {@endtemplate}
 */
@Composable
public inline fun <reified BlocA: BlocBase<BlocAState>,BlocAState:Any> Screen.BlocConsumer(
    noinline buildWhen:BlocBuilderCondition<BlocAState>?=null,
    noinline listenWhen: BlocListenerCondition<BlocAState>?=null,
    crossinline listener: @DisallowComposableCalls suspend (BlocAState) -> Unit,
    crossinline content:@Composable (BlocAState)->Unit)
{
    rememberProvidedBlocOf<BlocA>() ?.let { b->
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


/*
/// {@template bloc_consumer}
/// [BlocConsumer] exposes a [builder] and [listener] in order react to new
/// states.
/// [BlocConsumer] is analogous to a nested `BlocListener`
/// and `BlocBuilder` but reduces the amount of boilerplate needed.
/// [BlocConsumer] should only be used when it is necessary to both rebuild UI
/// and execute other reactions to state changes in the [bloc].
///
/// [BlocConsumer] takes a required `BlocWidgetBuilder`
/// and `BlocWidgetListener` and an optional [bloc],
/// `BlocBuilderCondition`, and `BlocListenerCondition`.
///
/// If the [bloc] parameter is omitted, [BlocConsumer] will automatically
/// perform a lookup using `BlocProvider` and the current `BuildContext`.
///
/// ```dart
/// BlocConsumer<BlocA, BlocAState>(
///   listener: (context, state) {
///     // do stuff here based on BlocA's state
///   },
///   builder: (context, state) {
///     // return widget here based on BlocA's state
///   }
/// )
/// ```
///
/// An optional [listenWhen] and [buildWhen] can be implemented for more
/// granular control over when [listener] and [builder] are called.
/// The [listenWhen] and [buildWhen] will be invoked on each [bloc] `state`
/// change.
/// They each take the previous `state` and current `state` and must return
/// a [bool] which determines whether or not the [builder] and/or [listener]
/// function will be invoked.
/// The previous `state` will be initialized to the `state` of the [bloc] when
/// the [BlocConsumer] is initialized.
/// [listenWhen] and [buildWhen] are optional and if they aren't implemented,
/// they will default to `true`.
///
/// ```dart
/// BlocConsumer<BlocA, BlocAState>(
///   listenWhen: (previous, current) {
///     // return true/false to determine whether or not
///     // to invoke listener with state
///   },
///   listener: (context, state) {
///     // do stuff here based on BlocA's state
///   },
///   buildWhen: (previous, current) {
///     // return true/false to determine whether or not
///     // to rebuild the widget with state
///   },
///   builder: (context, state) {
///     // return widget here based on BlocA's state
///   }
/// )
/// ```
/// {@endtemplate}
class BlocConsumer<B extends StateStreamable<S>, S> extends StatefulWidget {
  /// {@macro bloc_consumer}
  const BlocConsumer({
    Key? key,
    required this.builder,
    required this.listener,
    this.bloc,
    this.buildWhen,
    this.listenWhen,
  }) : super(key: key);

  /// The [bloc] that the [BlocConsumer] will interact with.
  /// If omitted, [BlocConsumer] will automatically perform a lookup using
  /// `BlocProvider` and the current `BuildContext`.
  final B? bloc;

  /// The [builder] function which will be invoked on each widget build.
  /// The [builder] takes the `BuildContext` and current `state` and
  /// must return a widget.
  /// This is analogous to the [builder] function in [StreamBuilder].
  final BlocWidgetBuilder<S> builder;

  /// Takes the `BuildContext` along with the [bloc] `state`
  /// and is responsible for executing in response to `state` changes.
  final BlocWidgetListener<S> listener;

  /// Takes the previous `state` and the current `state` and is responsible for
  /// returning a [bool] which determines whether or not to trigger
  /// [builder] with the current `state`.
  final BlocBuilderCondition<S>? buildWhen;

  /// Takes the previous `state` and the current `state` and is responsible for
  /// returning a [bool] which determines whether or not to call [listener] of
  /// [BlocConsumer] with the current `state`.
  final BlocListenerCondition<S>? listenWhen;

  @override
  State<BlocConsumer<B, S>> createState() => _BlocConsumerState<B, S>();
}

class _BlocConsumerState<B extends StateStreamable<S>, S>
    extends State<BlocConsumer<B, S>> {
  late B _bloc;

  @override
  void initState() {
    super.initState();
    _bloc = widget.bloc ?? context.read<B>();
  }

  @override
  void didUpdateWidget(BlocConsumer<B, S> oldWidget) {
    super.didUpdateWidget(oldWidget);
    final oldBloc = oldWidget.bloc ?? context.read<B>();
    final currentBloc = widget.bloc ?? oldBloc;
    if (oldBloc != currentBloc) _bloc = currentBloc;
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    final bloc = widget.bloc ?? context.read<B>();
    if (_bloc != bloc) _bloc = bloc;
  }

  @override
  Widget build(BuildContext context) {
    if (widget.bloc == null) {
      // Trigger a rebuild if the bloc reference has changed.
      // See https://github.com/felangel/bloc/issues/2127.
      context.select<B, bool>((bloc) => identical(_bloc, bloc));
    }
    return BlocBuilder<B, S>(
      bloc: _bloc,
      builder: widget.builder,
      buildWhen: (previous, current) {
        if (widget.listenWhen?.call(previous, current) ?? true) {
          widget.listener(context, current);
        }
        return widget.buildWhen?.call(previous, current) ?? true;
      },
    );
  }
}

 */