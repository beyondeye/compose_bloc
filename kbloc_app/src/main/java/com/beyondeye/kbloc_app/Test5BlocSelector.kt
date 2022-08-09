package com.beyondeye.kbloc_app

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beyondeye.kbloc.compose.bloc.*
import com.beyondeye.kbloc.compose.screen.Screen


class Test5BlocSelector: Screen {
    @Composable
    override fun Content() {
        Test5ScreenContent()
    }

    @Composable
    private fun Test5ScreenContent() {
        Column(modifier=Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                BlocProvider(create={cscope ->  MultiCounterBloc(cscope, Array(3){0})})
                {
                    //rememberProvidedBlocOf is similar to dependency injection: it retrieves the specified
                    //bloc type as defined by the closest enclosing BlocProvider
                    val b= rememberProvidedBlocOf<MultiCounterBloc>()?:return@BlocProvider
                    val onIncr0 = {
                        b.add(MultiAddEvent(0,1))
                    }
                    val onIncr1 = {
                        b.add(MultiAddEvent(1,1))
                    }
                    val onIncr2 = {
                        b.add(MultiAddEvent(2,1))
                    }
                    val onDecr0 = {
                        b.add(MultiSubEvent(0,1))
                    }
                    val onDecr1 = {
                        b.add(MultiSubEvent(1,1))
                    }
                    val onDecr2 = {
                        b.add(MultiSubEvent(2,1))
                    }

                    //2nd and third template argument types are inferred automatically
                    BlocSelector<MultiCounterBloc,_,_>(selectorFn = { state -> state.counter[0]})
                    { counterValue->
                        CounterControls("Counter[0]",counterValue, onDecr0,onIncr0)

                    }
                    Divider(modifier = Modifier.height(2.dp))
                    //2nd and third template argument types are inferred automatically
                    BlocSelector<MultiCounterBloc,_,_>(selectorFn = { state -> state.counter[1]})
                    { counterValue->
                        CounterControls("Counter[1]",counterValue, onDecr1,onIncr1)

                    }
                    Divider(modifier = Modifier.height(2.dp))
                    //2nd and third template argument types are inferred automatically
                    BlocSelector<MultiCounterBloc,_,_>(selectorFn = { state -> state.counter[2]})
                    { counterValue->
                        CounterControls("Counter[2]",counterValue, onDecr2,onIncr2)

                    }

                }

        }
    }
}

