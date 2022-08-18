package com.beyondeye.kbloc_app

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.beyondeye.kbloc.compose.BlocProvider
import com.beyondeye.kbloc.compose.BlocSelector
import com.beyondeye.kbloc.compose.rememberProvidedBlocOf
import com.beyondeye.kbloc.compose.reselect.SelectorFor


class Test6BlocSelectorWithAbstractSelector : Screen {
    @Composable
    override fun Content() {
        Test6ScreenContent()
    }

    @Composable
    private fun Test6ScreenContent() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BlocProvider(create = { cscope -> ABCCounterBloc(cscope, Array(3) { 0 }) })
            {
                //rememberProvidedBlocOf is similar to dependency injection: it retrieves the specified
                //bloc type as defined by the closest enclosing BlocProvider
                val bloc = rememberProvidedBlocOf<ABCCounterBloc>() ?: return@BlocProvider
                //TODO should I use remember for definition of all these callbacks?
                //     (also in other tests)
                val onIncr_a = remember {
                    {
                        bloc.add(MultiAddEvent(0, 1))
                    }
                }
                val onIncr_b = remember {
                    {
                        bloc.add(MultiAddEvent(1, 1))
                    }
                }
                val onIncr_c = remember {
                    {
                        bloc.add(MultiAddEvent(2, 1))
                    }
                }
                val onDecr_a = remember {
                    {
                        bloc.add(MultiSubEvent(0, 1))
                    }
                }
                val onDecr_b = remember {
                    {
                        bloc.add(MultiSubEvent(1, 1))
                    }
                }
                val onDecr_c = remember {
                    {
                        bloc.add(MultiSubEvent(2, 1))
                    }
                }
                val sel = SelectorFor<ABCCounterState>()
                BlocSelector(
                    bloc,
                    sel.withField { a }.withField { b }.withField { c }
                        .compute { a, b, c -> a + b + c }) {
                    Text("total count=$it")

                }
                //2nd and third template argument types are inferred automatically
                BlocSelector<ABCCounterBloc, _, _>(selector = sel.withSingleField { a })
                { counterValue ->
                    CounterControls("a", counterValue, onDecr_a, onIncr_a)

                }
                Divider(modifier = Modifier.height(2.dp))
                //if we explicitely specify the bloc parameter we don't need to write the template args at all!
                BlocSelector(bloc, sel.withSingleField { b })
                { counterValue ->
                    CounterControls("b", counterValue, onDecr_b, onIncr_b)

                }
                Divider(modifier = Modifier.height(2.dp))
                //if we explicitely specify the bloc parameter we don't need to write the template args at all!
                BlocSelector(bloc, sel.withSingleField { c })
                { counterValue ->
                    CounterControls("c", counterValue, onDecr_c, onIncr_c)
                }

            }

        }
    }
}

