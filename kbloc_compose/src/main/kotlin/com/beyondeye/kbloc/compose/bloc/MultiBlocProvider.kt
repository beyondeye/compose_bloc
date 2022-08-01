package com.beyondeye.kbloc.compose.bloc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import com.beyondeye.kbloc.compose.bloc.internals.BindBlocs
import com.beyondeye.kbloc.compose.bloc.internals.rememberBloc
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.core.BlocBase
import kotlinx.coroutines.CoroutineScope


/**
 * define multiple bloc providers to be available to some composable subtree
 * The syntax for defining the list is as follows:
 * MultiBlocProvider.BlocProvider { scope -> BlocA() }.BlocProvider { scope -> BlocB() }.forContent { content() }
 * where content() is a composable function() for which we want the blocs made available
 * Any number of BlocProvider definitions is supported
 */
@Composable
public fun Screen.MultiBlocProvider():_BlocProviderList {
    return _BlocProviderList(this)
}

/**
 * [blist] is a list of triples (Bloc:BlocBase<*>,bloc_tag:String,bloc_key:String)
 */
class _BlocProviderList(val screen: Screen, val blist:MutableList<Triple<BlocBase<*>,String,String>> = mutableListOf()) {
    @Composable
    public inline fun <reified BlocA: BlocBase<BlocAState>,BlocAState:Any> BlocProvider(
        tag: String? = null,
        crossinline create: @DisallowComposableCalls (cscope: CoroutineScope) -> BlocA
    )    : _BlocProviderList
    {
        val (b,bkey)=screen.rememberBloc(tag,create)
        blist.add(Triple(b,tag?:"",bkey))
        return this
    }
    @Composable
    public fun forContent(content:@Composable ()->Unit) {
        BindBlocs(blist,content)
        blist.clear()
    }
}

//
/*
import 'package:flutter/widgets.dart';
import 'package:flutter_bloc/src/bloc_provider.dart';
import 'package:provider/provider.dart';

/// {@template multi_bloc_provider}
/// Merges multiple [BlocProvider] widgets into one widget tree.
///
/// [MultiBlocProvider] improves the readability and eliminates the need
/// to nest multiple [BlocProvider]s.
///
/// By using [MultiBlocProvider] we can go from:
///
/// ```dart
/// BlocProvider<BlocA>(
///   create: (BuildContext context) => BlocA(),
///   child: BlocProvider<BlocB>(
///     create: (BuildContext context) => BlocB(),
///     child: BlocProvider<BlocC>(
///       create: (BuildContext context) => BlocC(),
///       child: ChildA(),
///     )
///   )
/// )
/// ```
///
/// to:
///
/// ```dart
/// MultiBlocProvider(
///   providers: [
///     BlocProvider<BlocA>(
///       create: (BuildContext context) => BlocA(),
///     ),
///     BlocProvider<BlocB>(
///       create: (BuildContext context) => BlocB(),
///     ),
///     BlocProvider<BlocC>(
///       create: (BuildContext context) => BlocC(),
///     ),
///   ],
///   child: ChildA(),
/// )
/// ```
///
/// [MultiBlocProvider] converts the [BlocProvider] list into a tree of nested
/// [BlocProvider] widgets.
/// As a result, the only advantage of using [MultiBlocProvider] is improved
/// readability due to the reduction in nesting and boilerplate.
/// {@endtemplate}
class MultiBlocProvider extends MultiProvider {
  /// {@macro multi_bloc_provider}
  MultiBlocProvider({
    Key? key,
    required List<BlocProviderSingleChildWidget> providers,
    required Widget child,
  }) : super(key: key, providers: providers, child: child);
}

 */