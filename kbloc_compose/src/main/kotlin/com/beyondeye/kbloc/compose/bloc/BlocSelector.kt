package com.beyondeye.kbloc.compose.bloc

import androidx.compose.runtime.*
import com.beyondeye.kbloc.compose.model.rememberBloc
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.core.BlocBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform

/// Signature for the `selector` function which
/// is responsible for returning a selected value, [T], based on [state].
typealias BlocWidgetSelector<S, T> = (S)->T

/**
 *
 * {@template bloc_selector}
 * [BlocSelector] is analogous to [BlocBuilder] but allows developers to
 * filter updates by selecting a new value based on the bloc state.
 * Unnecessary builds are prevented if the selected value does not change.
 *
 * **Note**: the selected value must be immutable in order for [BlocSelector]
 * to accurately determine whether [builder] should be called again.
 *
 * ```dart
 * BlocSelector<BlocA, BlocAState, SelectedState>(
 *   selector: (state) {
 *     // return selected state based on the provided state.
 *   },
 *   builder: (context, state) {
 *     // return widget here based on the selected state.
 *   },
 * )
 * ```
 * {@endtemplate}
 */
@Composable
inline fun <reified BlockA: BlocBase<BlockAState>,BlockAState:Any,BlockSelectedState : Any> Screen.BlocSelector(
    crossinline factory: @DisallowComposableCalls (cscope: CoroutineScope) -> BlockA,
    blocTag: String? = null,
    crossinline selector:BlocWidgetSelector<BlockAState,BlockSelectedState>,
    body:@Composable (BlockSelectedState)->Unit)
{
    val b = rememberBloc(blocTag,factory)
    val collect_scope= rememberCoroutineScope()
    val stream= b.stream.map{selector(it) }

    val state:BlockSelectedState by stream.collectAsState(selector(b.state),collect_scope.coroutineContext)
    body(state)
}

/**
 * since here we use an externally provided bloc, this is a composable that can be called
 * in any place not just a in [Screen.Content] member function
 */
@Composable
inline fun <reified BlockA:BlocBase<BlockAState>,BlockAState:Any,BlockSelectedState> BlocSelector(
    externallyProvidedBlock:BlockA,
    crossinline selector:BlocWidgetSelector<BlockAState,BlockSelectedState>,
    body:@Composable (BlockSelectedState)->Unit)
{
    val b =  remember { externallyProvidedBlock }
    val collect_scope= rememberCoroutineScope()
    val stream= b.stream.map{selector(it) }

    val state:BlockSelectedState by stream.collectAsState(selector(b.state),collect_scope.coroutineContext)
    body(state)
}


/*
import 'package:flutter/widgets.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

/// Signature for the `selector` function which
/// is responsible for returning a selected value, [T], based on [state].
typedef BlocWidgetSelector<S, T> = T Function(S state);

/// {@template bloc_selector}
/// [BlocSelector] is analogous to [BlocBuilder] but allows developers to
/// filter updates by selecting a new value based on the bloc state.
/// Unnecessary builds are prevented if the selected value does not change.
///
/// **Note**: the selected value must be immutable in order for [BlocSelector]
/// to accurately determine whether [builder] should be called again.
///
/// ```dart
/// BlocSelector<BlocA, BlocAState, SelectedState>(
///   selector: (state) {
///     // return selected state based on the provided state.
///   },
///   builder: (context, state) {
///     // return widget here based on the selected state.
///   },
/// )
/// ```
/// {@endtemplate}
class BlocSelector<B extends StateStreamable<S>, S, T> extends StatefulWidget {
  /// {@macro bloc_selector}
  const BlocSelector({
    Key? key,
    required this.selector,
    required this.builder,
    this.bloc,
  }) : super(key: key);

  /// The [bloc] that the [BlocSelector] will interact with.
  /// If omitted, [BlocSelector] will automatically perform a lookup using
  /// [BlocProvider] and the current [BuildContext].
  final B? bloc;

  /// The [builder] function which will be invoked
  /// when the selected state changes.
  /// The [builder] takes the [BuildContext] and selected `state` and
  /// must return a widget.
  /// This is analogous to the [builder] function in [BlocBuilder].
  final BlocWidgetBuilder<T> builder;

  /// The [selector] function which will be invoked on each widget build
  /// and is responsible for returning a selected value of type [T] based on
  /// the current state.
  final BlocWidgetSelector<S, T> selector;

  @override
  State<BlocSelector<B, S, T>> createState() => _BlocSelectorState<B, S, T>();
}

class _BlocSelectorState<B extends StateStreamable<S>, S, T>
    extends State<BlocSelector<B, S, T>> {
  late B _bloc;
  late T _state;

  @override
  void initState() {
    super.initState();
    _bloc = widget.bloc ?? context.read<B>();
    _state = widget.selector(_bloc.state);
  }

  @override
  void didUpdateWidget(BlocSelector<B, S, T> oldWidget) {
    super.didUpdateWidget(oldWidget);
    final oldBloc = oldWidget.bloc ?? context.read<B>();
    final currentBloc = widget.bloc ?? oldBloc;
    if (oldBloc != currentBloc) {
      _bloc = currentBloc;
      _state = widget.selector(_bloc.state);
    }
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    final bloc = widget.bloc ?? context.read<B>();
    if (_bloc != bloc) {
      _bloc = bloc;
      _state = widget.selector(_bloc.state);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (widget.bloc == null) {
      // Trigger a rebuild if the bloc reference has changed.
      // See https://github.com/felangel/bloc/issues/2127.
      context.select<B, bool>((bloc) => identical(_bloc, bloc));
    }
    return BlocListener<B, S>(
      bloc: _bloc,
      listener: (context, state) {
        final selectedState = widget.selector(state);
        if (_state != selectedState) setState(() => _state = selectedState);
      },
      child: widget.builder(context, _state),
    );
  }
}

 */