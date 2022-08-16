package com.beyondeye.kbloc.compose.bloc

import androidx.compose.runtime.*
import com.beyondeye.kbloc.compose.bloc.reselect.AbstractSelector
import com.beyondeye.kbloc.core.BlocBase
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

/**
 * Signature for the `selector` function which
 * is responsible for returning a selected value  T, based on S.
 * NOTE: this was named BlocWidgetSelector in flutter_bloc
 */
//typealias BlocSelectorFn<S, T> = @DisallowComposableCalls (S)->T

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
    crossinline selectorFn: @DisallowComposableCalls (BlocAState)->BlockSelectedState,
    content:@Composable (BlockSelectedState)->Unit)
{
    rememberProvidedBlocOf<BlocA>(blocTag)?.let { b->
        BlocSelectorForSelectorFn(b, selectorFn, content)
    }
}


/**
 */
@Composable
public inline fun <reified BlocA: BlocBase<BlocAState>,BlocAState:Any,BlockSelectedState : Any> BlocSelector(
    blocTag:String?=null,
    selector:AbstractSelector<BlocAState,BlockSelectedState>,
    content:@Composable (BlockSelectedState)->Unit)
{
    rememberProvidedBlocOf<BlocA>(blocTag)?.let { b->
        BlocSelectorForAbstractSelector(b, selector, content)
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
    crossinline selectorFn:@DisallowComposableCalls (BlocAState)->BlockSelectedState,
    content:@Composable (BlockSelectedState)->Unit)
{
    val b =  remember { externallyProvidedBlock }
    BlocSelectorForSelectorFn(b, selectorFn, content)
}

/**
 */
@Composable
public inline fun <reified BlocA:BlocBase<BlocAState>,BlocAState:Any,BlockSelectedState:Any> BlocSelector(
    externallyProvidedBlock:BlocA,
    selector:AbstractSelector<BlocAState,BlockSelectedState>,
    content:@Composable (BlockSelectedState)->Unit)
{
    val b =  remember { externallyProvidedBlock }
    BlocSelectorForAbstractSelector(b, selector, content)
}

//TODO: check again implementation of BlocSelectCore: how it is checked for new changed selected values:
//      by equality? by reference? it is checked at all? perhaps I should integrate with reduks selector
//      that give more flexibility in building the selector?
//      DOCUMENT THAT FOR MORE SOPHISTICATED CHECK SHOULD USE AbstractSelector
@PublishedApi
@Composable
internal inline fun <reified BlocA : BlocBase<BlocAState>, BlocAState : Any, BlockSelectedState : Any> BlocSelectorForSelectorFn(
    b: BlocA,
    crossinline selectoFn: @DisallowComposableCalls (BlocAState)->BlockSelectedState,
    content: @Composable (BlockSelectedState) -> Unit
) {
    val collect_scope = rememberCoroutineScope()
    val stream = b.stream.map { selectoFn(it) }
    val initialState=selectoFn(b.state)
    val state: BlockSelectedState by stream.collectAsState(
        initialState,
        collect_scope.coroutineContext
    )
    content(state)
}

/**
 */
@PublishedApi
@Composable
internal inline fun <reified BlocA : BlocBase<BlocAState>, BlocAState : Any, BlockSelectedState : Any> BlocSelectorForAbstractSelector(
    b: BlocA,
    selector: AbstractSelector<BlocAState, BlockSelectedState>,
    content: @Composable (BlockSelectedState) -> Unit
) {
    val collect_scope = rememberCoroutineScope()
    //store selector and reuse it on recomposition. no need to use rememberSaveable: when screen is
    // destroyed and rebuilt we probably want to trigger all our selector
    val sel = remember { selector }
    val stream = b.stream.mapNotNull { sel.getIfChangedIn(it) }
    val initialState=sel(b.state)
    val state: BlockSelectedState by stream.collectAsState(
        initialState,
        collect_scope.coroutineContext
    )
    content(state)
}


