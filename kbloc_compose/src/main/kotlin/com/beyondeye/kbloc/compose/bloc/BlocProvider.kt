package com.beyondeye.kbloc.compose.bloc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import com.beyondeye.kbloc.compose.bloc.internals.*
import com.beyondeye.kbloc.compose.bloc.internals.BlocStore.Companion.buildBlocBindingKey
import com.beyondeye.kbloc.compose.bloc.internals.BlocStore.Companion.getBlocKeyForUnboundBloc
import com.beyondeye.kbloc.compose.bloc.internals.LocalBlocBindings
import com.beyondeye.kbloc.compose.bloc.internals.rememberBloc
import com.beyondeye.kbloc.compose.model.ScreenModel
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.BlocBase
import kotlinx.coroutines.CoroutineScope

/**
 *  [BlocProvider] is a composable which provides a [Bloc] to its child composable [content] and
 *  all the associated composable tree.
 *  The bloc can be retrieved it by calls to [rememberProvidedBlocOf]
 *  It is used as a dependency injection (DI) configuration so that a single instance
 *  of a [Bloc] can be provided to multiple child composables within a subtree.
 *  BlocProvider is defined as an extension method of [Screen] because the lifecycle of the [Bloc]
 *  is bound to the the lifecycle of that [Screen] (similar to [ScreenModel]).
 *  i.e. when the [Screen] is disposed so will be all blocs defined with [BlocProvider]  in that screen
 *  (the [Bloc.close] method will be called and associated coroutine scope [Bloc.cscope] will be canceled)
 *
 * An optional [blocTag] parameter can be specified in order to identify a specific
 * bloc instance in case there is more than one instance of a bloc of the same type
 * to be registered to the current composable subtree
 * [blocTag] parameter is not present in the original flutter_bloc implementation
 * *
 *  NOTE: in the original flutter_bloc implementation there is an option to create the provided
 *        bloc lazily. There is current no such option in this implementation
 */
@Composable
inline fun <reified BlocA: BlocBase<*>> Screen.BlocProvider(
    blocTag: String? = null,
    crossinline create: @DisallowComposableCalls (cscope: CoroutineScope) -> BlocA,
    crossinline content:@Composable ()->Unit)
{
    val (b,bkey)=rememberBloc(blocTag,create)

    BindBloc(b,blocTag,bkey) {
        content()
    }
}



/**
 * same as previous method but with explicitely specified bloc instance [externallyProvidedBlock]
 * not a bloc created and bound to the current screen. Lifecycle of this bloc will be managed
 * in the Screen it is associated with, not here.
 *
 * NOTE: in flutter_bloc this method was called BlocProvider.value
 */
@Composable
inline fun <reified BlocA: BlocBase<*>> BlocProvider(
    blocTag: String? = null,
    externallyProvidedBlock:BlocA,
    crossinline content:@Composable ()->Unit)
{
    val b=remember { externallyProvidedBlock }
    val bkey = getBlocKeyForUnboundBloc<BlocA>(blocTag)
    BindBloc(b,blocTag,bkey) {
        content()
    }
}

/**
 * Use this method to obtain a bloc that was previously configured with [BlocProvider]
 * in a parent composable
 *
 * An optional [blocTag] parameter can be specified in order to identify a specific
 * bloc instance in case there is more than one instance of a bloc of same type
 * registered for the current composable subtree
 * [blocTag] parameter is not present in the original flutter_bloc implementation *
 *
 * NOTE: in flutter_bloc the the original method was BlocProvider.of<Type>. we have renamed to reflect
 * the usage of remember that is specific to Compose.
 * NOTE: in flutter_bloc, when the required [Bloc] is not found an exception is thrown.
 *       In this implementation instead we return null
 */
@Composable
inline fun <reified BlocA: BlocBase<*>>
        rememberProvidedBlocOf(blocTag:String?=null):BlocA?
{
    val curBindings=LocalBlocBindings.current
    val store = LocalBlocStoreOwner.current.blocStore
    return remember(curBindings) { //recalculate if curBindings change
        val bkey=curBindings.bindingMaps[buildBlocBindingKey<BlocA>(blocTag)]
        store.blocs.get(bkey) as BlocA?
    }
}
