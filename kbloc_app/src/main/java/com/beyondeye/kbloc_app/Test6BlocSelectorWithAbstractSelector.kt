package com.beyondeye.kbloc_app

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beyondeye.kbloc.compose.bloc.*
import com.beyondeye.kbloc.compose.bloc.reselect.SelectorBuilder
import com.beyondeye.kbloc.compose.screen.Screen


class Test6BlocSelectorWithAbstractSelector: Screen {
    @Composable
    override fun Content() {
        Test6ScreenContent()
    }

    @Composable
    private fun Test6ScreenContent() {
        Column(modifier=Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                BlocProvider(create={cscope ->  ABCCounterBloc(cscope, Array(3){0})})
                {
                    //rememberProvidedBlocOf is similar to dependency injection: it retrieves the specified
                    //bloc type as defined by the closest enclosing BlocProvider
                    val bloc= rememberProvidedBlocOf<ABCCounterBloc>()?:return@BlocProvider
                    val onIncr_a = {
                        bloc.add(MultiAddEvent(0,1))
                    }
                    val onIncr_b = {
                        bloc.add(MultiAddEvent(1,1))
                    }
                    val onIncr_c = {
                        bloc.add(MultiAddEvent(2,1))
                    }
                    val onDecr_a = {
                        bloc.add(MultiSubEvent(0,1))
                    }
                    val onDecr_b = {
                        bloc.add(MultiSubEvent(1,1))
                    }
                    val onDecr_c = {
                        bloc.add(MultiSubEvent(2,1))
                    }

                    //2nd and third template argument types are inferred automatically
                    BlocSelector<ABCCounterBloc,_,_>(select = { SelectorBuilder<ABCCounterState>().withSingleField { a } })
                    { counterValue->
                        CounterControls("a",counterValue, onDecr_a,onIncr_a)

                    }
                    Divider(modifier = Modifier.height(2.dp))
                    //2nd and third template argument types are inferred automatically
                    BlocSelector<ABCCounterBloc,_,_>(select = { SelectorBuilder<ABCCounterState>().withSingleField { b } })
                    { counterValue->
                        CounterControls("b",counterValue, onDecr_b,onIncr_b)

                    }
                    Divider(modifier = Modifier.height(2.dp))
                    //2nd and third template argument types are inferred automatically
                    BlocSelector<ABCCounterBloc,_,_>(select = { SelectorBuilder<ABCCounterState>().withSingleField { c } })
                    { counterValue->
                        CounterControls("c",counterValue, onDecr_c,onIncr_c)

                    }

                }

        }
    }
}

