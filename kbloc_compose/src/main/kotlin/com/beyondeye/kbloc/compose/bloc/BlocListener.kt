package com.beyondeye.kbloc.compose.bloc

import androidx.compose.runtime.*
import com.beyondeye.kbloc.compose.bloc.internals.rememberBloc
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.core.BlocBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

/**
* Signature for the `listenWhen` function which takes the previous `state`
* and the current `state` and is responsible for returning a [bool] which
* determines whether or not to call [BlocWidgetListener] of [BlocListener]
* with the current `state`.
 * todo [BlocBuilderCondition] and [BlocListenerCondition] as exactly the same: merge them
 */
typealias BlocListenerCondition<S> =(previousState:S?,currentState:S)->Boolean


/**
 * todo [listenWhenFilter] and [buildWhenFilter] are exaclty the same: merge them
 */
fun <BlocAState>listenWhenFilter(srcFlow: Flow<BlocAState>, listenWhen: BlocListenerCondition<BlocAState>): Flow<BlocAState> {
    var prevState:BlocAState?=null
    return  srcFlow.transform { curState->
        if(listenWhen(prevState,curState)) {
            emit(curState)
        }
        prevState=curState
    }
}

/**
* {@template bloc_listener}
* Takes a [BlocWidgetListener] and an optional [bloc] and invokes
* the [listener] in response to `state` changes in the [bloc].
* It should be used for functionality that needs to occur only in response to
* a `state` change such as navigation, showing a `SnackBar`, showing
* a `Dialog`, etc...
* The [listener] is guaranteed to only be called once for each `state` change
* unlike the `builder` in `BlocBuilder`.
*
* If the [bloc] parameter is omitted, [BlocListener] will automatically
* perform a lookup using [BlocProvider] and the current `BuildContext`.
*
* ```dart
* BlocListener<BlocA, BlocAState>(
*   listener: (context, state) {
*     // do stuff here based on BlocA's state
*   },
*   child: Container(),
* )
* ```
* Only specify the [bloc] if you wish to provide a [bloc] that is otherwise
* not accessible via [BlocProvider] and the current `BuildContext`.
*
* ```dart
* BlocListener<BlocA, BlocAState>(
*   value: blocA,
*   listener: (context, state) {
*     // do stuff here based on BlocA's state
*   },
*   child: Container(),
* )
* ```
* {@endtemplate}
*
* {@template bloc_listener_listen_when}
* An optional [listenWhen] can be implemented for more granular control
* over when [listener] is called.
* [listenWhen] will be invoked on each [bloc] `state` change.
* [listenWhen] takes the previous `state` and current `state` and must
* return a [bool] which determines whether or not the [listener] function
* will be invoked.
* The previous `state` will be initialized to the `state` of the [bloc]
* when the [BlocListener] is initialized.
* [listenWhen] is optional and if omitted, it will default to `true`.
*
* ```dart
* BlocListener<BlocA, BlocAState>(
*   listenWhen: (previous, current) {
*     // return true/false to determine whether or not
*     // to invoke listener with state
*   },
*   listener: (context, state) {
*     // do stuff here based on BlocA's state
*   }
*   child: Container(),
* )
* ```
* {@endtemplate}
 */
@Composable
public inline fun <reified BlocA: BlocBase<BlocAState>,BlocAState:Any> Screen.BlocListener(
    crossinline factory: @DisallowComposableCalls (cscope: CoroutineScope) -> BlocA,
    blocTag: String? = null,
    noinline listenWhen: BlocListenerCondition<BlocAState>?=null,
    crossinline listener: @DisallowComposableCalls suspend (BlocAState) -> Unit,
    body:@Composable ()->Unit)
{
    //TODO: in the original code if b changes, then a recomposition is triggered with the new bloc
    //      and new bloc state
    val (b,bkey) = rememberBloc(blocTag,factory)
    BlocListenerCore(b,bkey, listenWhen, listener, body)
}

/**
 * since here we use an externally provided bloc, this is a composable that can be called
 * in any place not just a in [Screen.Content] member function
 */
@Composable
public inline fun <reified BlocA: BlocBase<BlocAState>,BlocAState:Any> BlocListener(
    externallyProvidedBlock:BlocA,
    noinline listenWhen: BlocListenerCondition<BlocAState>?=null,
    crossinline listener: @DisallowComposableCalls suspend (BlocAState) -> Unit,
    body:@Composable ()->Unit)
{
    val b =  remember { externallyProvidedBlock }
    BlocListenerCore(b,null, listenWhen, listener, body)
}

@PublishedApi
@Composable
internal inline fun <reified BlocA : BlocBase<BlocAState>, BlocAState : Any> BlocListenerCore(
    b: BlocA,
    bkey: String?,
    noinline listenWhen: BlocListenerCondition<BlocAState>?,
    crossinline listener: @DisallowComposableCalls suspend (BlocAState) -> Unit,
    body: @Composable () -> Unit
) {
    val collect_scope = rememberCoroutineScope()
    val stream = if (listenWhen == null) b.stream else {
        listenWhenFilter(b.stream, listenWhen)
    }
    val state: BlocAState by stream.collectAsState(b.state, collect_scope.coroutineContext)
    //TODO according to the documentation of LaunchedEffect, what I am doing here, if I understand
    // the docs correclty, that is o (re-)launch ongoing tasks in response to callback
    // * events by way of storing callback data in [MutableState] passed to [key]
    // is something that should no be done: need to understand better
    LaunchedEffect(state) {
        listener(state)
    }
    //TODO if bkey!=null then bind bloc
    body()
}



/*
import 'dart:async';

import 'package:flutter/widgets.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:provider/single_child_widget.dart';

/// Mixin which allows `MultiBlocListener` to infer the types
/// of multiple [BlocListener]s.
mixin BlocListenerSingleChildWidget on SingleChildWidget {}

/// Signature for the `listener` function which takes the `BuildContext` along
/// with the `state` and is responsible for executing in response to
/// `state` changes.
typedef BlocWidgetListener<S> = void Function(BuildContext context, S state);


/// {@template bloc_listener}
/// Takes a [BlocWidgetListener] and an optional [bloc] and invokes
/// the [listener] in response to `state` changes in the [bloc].
/// It should be used for functionality that needs to occur only in response to
/// a `state` change such as navigation, showing a `SnackBar`, showing
/// a `Dialog`, etc...
/// The [listener] is guaranteed to only be called once for each `state` change
/// unlike the `builder` in `BlocBuilder`.
///
/// If the [bloc] parameter is omitted, [BlocListener] will automatically
/// perform a lookup using [BlocProvider] and the current `BuildContext`.
///
/// ```dart
/// BlocListener<BlocA, BlocAState>(
///   listener: (context, state) {
///     // do stuff here based on BlocA's state
///   },
///   child: Container(),
/// )
/// ```
/// Only specify the [bloc] if you wish to provide a [bloc] that is otherwise
/// not accessible via [BlocProvider] and the current `BuildContext`.
///
/// ```dart
/// BlocListener<BlocA, BlocAState>(
///   value: blocA,
///   listener: (context, state) {
///     // do stuff here based on BlocA's state
///   },
///   child: Container(),
/// )
/// ```
/// {@endtemplate}
///
/// {@template bloc_listener_listen_when}
/// An optional [listenWhen] can be implemented for more granular control
/// over when [listener] is called.
/// [listenWhen] will be invoked on each [bloc] `state` change.
/// [listenWhen] takes the previous `state` and current `state` and must
/// return a [bool] which determines whether or not the [listener] function
/// will be invoked.
/// The previous `state` will be initialized to the `state` of the [bloc]
/// when the [BlocListener] is initialized.
/// [listenWhen] is optional and if omitted, it will default to `true`.
///
/// ```dart
/// BlocListener<BlocA, BlocAState>(
///   listenWhen: (previous, current) {
///     // return true/false to determine whether or not
///     // to invoke listener with state
///   },
///   listener: (context, state) {
///     // do stuff here based on BlocA's state
///   }
///   child: Container(),
/// )
/// ```
/// {@endtemplate}
class BlocListener<B extends StateStreamable<S>, S>
    extends BlocListenerBase<B, S> with BlocListenerSingleChildWidget {
  /// {@macro bloc_listener}
  /// {@macro bloc_listener_listen_when}
  const BlocListener({
    Key? key,
    required BlocWidgetListener<S> listener,
    B? bloc,
    BlocListenerCondition<S>? listenWhen,
    Widget? child,
  }) : super(
          key: key,
          child: child,
          listener: listener,
          bloc: bloc,
          listenWhen: listenWhen,
        );
}

/// {@template bloc_listener_base}
/// Base class for widgets that listen to state changes in a specified [bloc].
///
/// A [BlocListenerBase] is stateful and maintains the state subscription.
/// The type of the state and what happens with each state change
/// is defined by sub-classes.
/// {@endtemplate}
abstract class BlocListenerBase<B extends StateStreamable<S>, S>
    extends SingleChildStatefulWidget {
  /// {@macro bloc_listener_base}
  const BlocListenerBase({
    Key? key,
    required this.listener,
    this.bloc,
    this.child,
    this.listenWhen,
  }) : super(key: key, child: child);

  /// The widget which will be rendered as a descendant of the
  /// [BlocListenerBase].
  final Widget? child;

  /// The [bloc] whose `state` will be listened to.
  /// Whenever the [bloc]'s `state` changes, [listener] will be invoked.
  final B? bloc;

  /// The [BlocWidgetListener] which will be called on every `state` change.
  /// This [listener] should be used for any code which needs to execute
  /// in response to a `state` change.
  final BlocWidgetListener<S> listener;

  /// {@macro bloc_listener_listen_when}
  final BlocListenerCondition<S>? listenWhen;

  @override
  SingleChildState<BlocListenerBase<B, S>> createState() =>
      _BlocListenerBaseState<B, S>();
}

class _BlocListenerBaseState<B extends StateStreamable<S>, S>
    extends SingleChildState<BlocListenerBase<B, S>> {
  StreamSubscription<S>? _subscription;
  late B _bloc;
  late S _previousState;

  @override
  void initState() {
    super.initState();
    _bloc = widget.bloc ?? context.read<B>();
    _previousState = _bloc.state;
    _subscribe();
  }

  @override
  void didUpdateWidget(BlocListenerBase<B, S> oldWidget) {
    super.didUpdateWidget(oldWidget);
    final oldBloc = oldWidget.bloc ?? context.read<B>();
    final currentBloc = widget.bloc ?? oldBloc;
    if (oldBloc != currentBloc) {
      if (_subscription != null) {
        _unsubscribe();
        _bloc = currentBloc;
        _previousState = _bloc.state;
      }
      _subscribe();
    }
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    final bloc = widget.bloc ?? context.read<B>();
    if (_bloc != bloc) {
      if (_subscription != null) {
        _unsubscribe();
        _bloc = bloc;
        _previousState = _bloc.state;
      }
      _subscribe();
    }
  }

  @override
  Widget buildWithChild(BuildContext context, Widget? child) {
    assert(
      child != null,
      '''${widget.runtimeType} used outside of MultiBlocListener must specify a child''',
    );
    if (widget.bloc == null) {
      // Trigger a rebuild if the bloc reference has changed.
      // See https://github.com/felangel/bloc/issues/2127.
      context.select<B, bool>((bloc) => identical(_bloc, bloc));
    }
    return child!;
  }

  @override
  void dispose() {
    _unsubscribe();
    super.dispose();
  }

  void _subscribe() {
    _subscription = _bloc.stream.listen((state) {
      if (widget.listenWhen?.call(_previousState, state) ?? true) {
        widget.listener(context, state);
      }
      _previousState = state;
    });
  }

  void _unsubscribe() {
    _subscription?.cancel();
    _subscription = null;
  }
}

 */