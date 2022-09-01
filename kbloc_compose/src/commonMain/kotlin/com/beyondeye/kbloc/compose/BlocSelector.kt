package com.beyondeye.kbloc.compose

import androidx.compose.runtime.*
import com.beyondeye.kbloc.compose.lifecycle.mp_collectAsStateWithLifecycle
import com.beyondeye.kbloc.compose.reselect.AbstractSelector
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
 * [BlocSelector] is analogous to [BlocBuilder] but allows to select parts of the full Bloc state,
 * or in general some value derived from one or more fields of the full Bloc state. This is
 * the value that will be passed to the [content] composable, and recomposition will be triggered
 * not by change of the full bloc state but instead of this derived value.
 *
 * An optional [blocTag] parameter can be specified in order to identify a specific
 * bloc instance in the case where there is more than one instance of a bloc of the same type
 * registered for the current composable subtree (see [BlocProvider])
 * [blocTag] parameter is not present in the original flutter_bloc implementation
 *
 * **Note**: the selected value must be immutable in order for [BlocSelector]
 * to accurately determine whether [content] should be called again.
 * TODO understand why and check if BlocSelector work as expected
 *
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
    selector: AbstractSelector<BlocAState, BlockSelectedState>,
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
    selector: AbstractSelector<BlocAState, BlockSelectedState>,
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
    val initialState= remember { selectoFn(b.state) }
    val stream = remember { b.stream.map { selectoFn(it) } }

    //collection automatically paused when activity paused
    val state: BlockSelectedState by stream.mp_collectAsStateWithLifecycle(
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
    val initialState=remember { sel(b.state) }
    val stream = remember { b.stream.mapNotNull { sel.getIfChangedIn(it) } }
    //collection automatically paused when activity paused
    val state: BlockSelectedState by stream.mp_collectAsStateWithLifecycle(
        initialState,
        collect_scope.coroutineContext
    )
    content(state)
}


