package com.beyondeye.kbloc.compose.bloc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.beyondeye.kbloc.compose.model.coroutineScope
import com.beyondeye.kbloc.compose.model.rememberBloc
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.core.BlocBase

class BlocProvider//cannot be instantiated
private constructor() {
    companion object {
        inline fun <reified BlockA: BlocBase<*>> of() {

        }
        @Composable
        inline fun <reified BlockA: BlocBase<BlockAState>,BlockAState:Any> value(
            tag: String? = null,
            value:BlockA,
            body:@Composable (BlockAState)->Unit)
        {
         /*
            val b = if(block!=null) remember { block } else rememberBloc(tag,factory)
            //TODO is this correct? I want to stream collection to be cancelled when a bloc is closed
            //TODO use instead the bloc coroutine scope field?
            val state =b.stream.collectAsState(context=b.coroutineScope().coroutineContext)
            body(state.value)

             */
        }


    }
}

/**
        BlocProvider is a composable which provides a bloc to its children via BlocProvider.of<T>(context).
         It is used as a dependency injection (DI) configuration so that a single instance
         of a bloc can be provided to multiple child composables within a subtree.
       By default, BlocProvider will create the bloc lazily, meaning create will get executed when
        the bloc is looked up via BlocProvider.of<BlocA>(context).
        BlocProvider is defined as extension method of [Screen] because its lifecycle is associated
        to that of the screen. i.e. when the Screen is disposes so will be all blocs defined
        with BlocProvider in that screen
        If instead BlocProvider is used to provide an existing block to another navigation route
        then use [BlocProviderValue] method. In this case
 */
@Composable
inline fun <reified BlockA: BlocBase<BlockAState>,BlockAState:Any> Screen.BlocProvider(
    tag: String? = null,
    /**
     * By default, BlocProvider will create the bloc lazily, meaning create will get executed when the bloc is looked up via BlocProvider.of<BlocA>(context).
     */
    lazy:Boolean=false,
    crossinline create: @DisallowComposableCalls () -> BlockA,
    body:@Composable (BlockAState)->Unit)
{
    /*
    val b = if(block!=null) remember { block } else rememberBloc(tag,factory)
    //TODO is this correct? I want to stream collection to be cancelled when a bloc is closed
    //TODO use instead the bloc coroutine scope field?
    val state =b.stream.collectAsState(context=b.coroutineScope().coroutineContext)
    body(state.value)

     */
}




//
/*
import 'package:bloc/bloc.dart';
import 'package:flutter/widgets.dart';
import 'package:provider/provider.dart';
import 'package:provider/single_child_widget.dart';

/// Mixin which allows `MultiBlocProvider` to infer the types
/// of multiple [BlocProvider]s.
mixin BlocProviderSingleChildWidget on SingleChildWidget {}

/// {@template bloc_provider}
/// Takes a [Create] function that is responsible for
/// creating the [Bloc] or [Cubit] and a [child] which will have access
/// to the instance via `BlocProvider.of(context)`.
/// It is used as a dependency injection (DI) widget so that a single instance
/// of a [Bloc] or [Cubit] can be provided to multiple widgets within a subtree.
///
/// ```dart
/// BlocProvider(
///   create: (BuildContext context) => BlocA(),
///   child: ChildA(),
/// );
/// ```
///
/// It automatically handles closing the instance when used with [Create].
/// By default, [Create] is called only when the instance is accessed.
/// To override this behavior, set [lazy] to `false`.
///
/// ```dart
/// BlocProvider(
///   lazy: false,
///   create: (BuildContext context) => BlocA(),
///   child: ChildA(),
/// );
/// ```
///
/// {@endtemplate}
class BlocProvider<T extends StateStreamableSource<Object?>>
    extends SingleChildStatelessWidget with BlocProviderSingleChildWidget {
  /// {@macro bloc_provider}
  const BlocProvider({
    Key? key,
    required Create<T> create,
    this.child,
    this.lazy = true,
  })  : _create = create,
        _value = null,
        super(key: key, child: child);

  /// Takes a [value] and a [child] which will have access to the [value] via
  /// `BlocProvider.of(context)`.
  /// When `BlocProvider.value` is used, the [Bloc] or [Cubit]
  /// will not be automatically closed.
  /// As a result, `BlocProvider.value` should only be used for providing
  /// existing instances to new subtrees.
  ///
  /// A new [Bloc] or [Cubit] should not be created in `BlocProvider.value`.
  /// New instances should always be created using the
  /// default constructor within the [Create] function.
  ///
  /// ```dart
  /// BlocProvider.value(
  ///   value: BlocProvider.of<BlocA>(context),
  ///   child: ScreenA(),
  /// );
  /// ```
  const BlocProvider.value({
    Key? key,
    required T value,
    this.child,
  })  : _value = value,
        _create = null,
        lazy = true,
        super(key: key, child: child);

  /// Widget which will have access to the [Bloc] or [Cubit].
  final Widget? child;

  /// Whether the [Bloc] or [Cubit] should be created lazily.
  /// Defaults to `true`.
  final bool lazy;

  final Create<T>? _create;

  final T? _value;

  /// Method that allows widgets to access a [Bloc] or [Cubit] instance
  /// as long as their `BuildContext` contains a [BlocProvider] instance.
  ///
  /// If we want to access an instance of `BlocA` which was provided higher up
  /// in the widget tree we can do so via:
  ///
  /// ```dart
  /// BlocProvider.of<BlocA>(context);
  /// ```
  static T of<T extends StateStreamableSource<Object?>>(
    BuildContext context, {
    bool listen = false,
  }) {
    try {
      return Provider.of<T>(context, listen: listen);
    } on ProviderNotFoundException catch (e) {
      if (e.valueType != T) rethrow;
      throw FlutterError(
        '''
        BlocProvider.of() called with a context that does not contain a $T.
        No ancestor could be found starting from the context that was passed to BlocProvider.of<$T>().

        This can happen if the context you used comes from a widget above the BlocProvider.

        The context used was: $context
        ''',
      );
    }
  }

  @override
  Widget buildWithChild(BuildContext context, Widget? child) {
    assert(
      child != null,
      '$runtimeType used outside of MultiBlocProvider must specify a child',
    );
    final value = _value;
    return value != null
        ? InheritedProvider<T>.value(
            value: value,
            startListening: _startListening,
            lazy: lazy,
            child: child,
          )
        : InheritedProvider<T>(
            create: _create,
            dispose: (_, bloc) => bloc.close(),
            startListening: _startListening,
            child: child,
            lazy: lazy,
          );
  }

  static VoidCallback _startListening(
    InheritedContext<StateStreamable?> e,
    StateStreamable value,
  ) {
    final subscription = value.stream.listen(
      (dynamic _) => e.markNeedsNotifyDependents(),
    );
    return subscription.cancel;
  }
}

 */