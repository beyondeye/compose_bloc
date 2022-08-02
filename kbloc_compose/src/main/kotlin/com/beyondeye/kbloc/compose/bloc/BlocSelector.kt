package com.beyondeye.kbloc.compose.bloc

import androidx.compose.runtime.*
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.core.BlocBase
import kotlinx.coroutines.flow.map

/**
 * Signature for the `selector` function which
 * is responsible for returning a selected value  T, based on S.
 * NOTE: this was named BlocWidgetSelector in flutter_bloc
 * TODO: integrate BlocSelectorFn with reduks selectors that automatically check for state changes before emitting selected state
 */
typealias BlocSelectorFn<S, T> = (S)->T

/**
 *
 * [BlocSelector] is analogous to [BlocBuilder] but allows developers to
 * filter updates by selecting a new value based on the bloc state.
 * Unnecessary recompositions of [content] are prevented if the selected value does not change.
 *
 * An optional [blocTag] parameter can be specified in order to identify a specific
 * bloc instance in case there is more than one instance of a bloc of same type
 * registered for the current composable subtree (see [BlocProvider])
 * [blocTag] parameter is not present in the original flutter_bloc implementation
 *
 * **Note**: the selected value must be immutable in order for [BlocSelector]
 * to accurately determine whether [content] should be called again.
 * TODO understand why and check if BlocSelector work as expected
 *
 * {@endtemplate}
 */
@Composable
public inline fun <reified BlocA: BlocBase<BlocAState>,BlocAState:Any,BlockSelectedState : Any> BlocSelector(
    blocTag:String?=null,
    crossinline selector:BlocSelectorFn<BlocAState,BlockSelectedState>,
    content:@Composable (BlockSelectedState)->Unit)
{
    rememberProvidedBlocOf<BlocA>(blocTag)?.let { b->
        BlocSelectorCore(b, selector, content)
    }
}

/**
 * same as previous method but with explicitely specified bloc instance [externallyProvidedBlock]
 * not retrieved implicitely from current registered blocs in the current composable subtree
 * see [BlocProvider]
 */
@Composable
public inline fun <reified BlocA:BlocBase<BlocAState>,BlocAState:Any,BlockSelectedState:Any> BlocSelector(
    externallyProvidedBlock:BlocA,
    crossinline selector:BlocSelectorFn<BlocAState,BlockSelectedState>,
    content:@Composable (BlockSelectedState)->Unit)
{
    val b =  remember { externallyProvidedBlock }
    BlocSelectorCore(b, selector, content)
}

//TODO: check again implementation of BlocSelectCore: how it is checked for new changed selected values:
//      by equality? by reference? it is checked at all? perhaps I should integrate with reduks selector
//      that give more flexibility in building the selector?
@PublishedApi
@Composable
internal inline fun <reified BlocA : BlocBase<BlocAState>, BlocAState : Any, BlockSelectedState : Any> BlocSelectorCore(
    b: BlocA,
    crossinline selector: BlocSelectorFn<BlocAState, BlockSelectedState>,
    content: @Composable (BlockSelectedState) -> Unit
) {
    val collect_scope = rememberCoroutineScope()
    val stream = b.stream.map { selector(it) }
    val initialState=selector(b.state)
    val state: BlockSelectedState by stream.collectAsState(
        initialState,
        collect_scope.coroutineContext
    )
    content(state)
}


