package com.beyondeye.kbloc.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import com.beyondeye.kbloc.compose.internal.BindBloc
import com.beyondeye.kbloc.compose.internal.BlocStore.Companion.buildBlocBindingKey
import com.beyondeye.kbloc.compose.internal.BlocStore.Companion.getBlocKeyForUnboundBloc
import com.beyondeye.kbloc.compose.internal.LocalBlocBindings
import com.beyondeye.kbloc.compose.internal.LocalBlocStoreOwner
import com.beyondeye.kbloc.compose.internal.rememberBloc
import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.BlocBase
import kotlinx.coroutines.CoroutineScope

/**
 *  [BlocProvider] is a composable which provides a [Bloc] to its child composable [content] and
 *  all the associated composable tree.
 *  The bloc can be retrieved  by calls to [rememberProvidedBlocOf]
 *  It is used as a dependency injection (DI) configuration so that a single instance
 *  of a [Bloc] can be provided to multiple child composables within a subtree.
 *  BlocProvider is defined as an extension method of [Screen] because the lifecycle of the provided [Bloc]
 *  is bound to the the lifecycle of that [Screen] (similar to [ScreenModel]).
 *  i.e. when the [Screen] is disposed so will be all blocs defined with [BlocProvider]  in that screen
 *  (the [Bloc.close] method will be called and associated coroutine scope [Bloc.cscope] will be canceled)
 *
 *  An optional [blocTag] parameter can be specified in order to identify a specific
 *  bloc instance in case there is more than one instance of a bloc of the same type
 *  to be registered to the current composable subtree
 *  [blocTag] parameter is not present in the original flutter_bloc implementation
 *
 *  The [create] factory method is used to create the Bloc instance (or retrieving it with
 *  some Dependency Injection library). The cscope parameter passed to [create] is a CoroutineScope
 *  bound to the current screen and will be cancelled when the Screen is disposed. It is meant to be
 *  passed to the Bloc constructor, that always requires such parameter
 *
 *   NOTE: in the original flutter_bloc implementation there is an option to create the provided
 *         bloc lazily. There is currently no such option in this implementation
 */
@Composable
public inline fun <reified BlocA: BlocBase<*>> Screen.BlocProvider(
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
public inline fun <reified BlocA: BlocBase<*>> BlocProvider(
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
 *
 * [blocTag] parameter is not present in the original flutter_bloc implementation *
 *
 * NOTE: in flutter_bloc the  original method was called BlocProvider.of<Type>.
 * we have renamed ot to reflect the usage of remember that is specific to Compose.
 * NOTE: in flutter_bloc, when the requested [Bloc] is not found an exception is thrown.
 *       In this implementation instead we return null
 */
@Composable
public inline fun <reified BlocA: BlocBase<*>>
        rememberProvidedBlocOf(blocTag:String?=null):BlocA?
{
    val curBindings=LocalBlocBindings.current
    val store = LocalBlocStoreOwner.current.blocStore
    return remember(curBindings) { //recalculate if curBindings change
        val bkey=curBindings.bindingMaps[buildBlocBindingKey<BlocA>(blocTag)]
        store.blocs.value.get(bkey) as BlocA?
    }
}
