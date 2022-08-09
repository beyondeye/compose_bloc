package com.beyondeye.kbloc_app

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beyondeye.kbloc.compose.bloc.BlocBuilder
import com.beyondeye.kbloc.compose.bloc.BlocProvider
import com.beyondeye.kbloc.compose.bloc.MultiBlocProvider
import com.beyondeye.kbloc.compose.bloc.rememberProvidedBlocOf
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.core.LOGTAG


class Test4MultiBlocProviderScreen: Screen {
    @Composable
    override fun Content() {
        Test4ScreenContent()
    }

    @Composable
    private fun Test4ScreenContent() {
        Column(modifier=Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            MultiBlocProvider()
                .BlocProvider<CounterBloc>("cnt1") {cscope ->  CounterBloc(cscope,1)}
                .BlocProvider<CounterBloc>("cnt10") {cscope ->  CounterBloc(cscope,10)}
                .BlocProvider<CounterBloc>("cnt20") {cscope ->  CounterBloc(cscope,20)}
                .ForContent {
                    //rememberProvidedBlocOf is similar to dependency injection: it retrieves the specified
                    //bloc type as defined by the closest enclosing BlocProvider
                    val b1= rememberProvidedBlocOf<CounterBloc>("cnt1")?:return@ForContent
                    val b10= rememberProvidedBlocOf<CounterBloc>("cnt10")?:return@ForContent
                    val b20= rememberProvidedBlocOf<CounterBloc>("cnt20")?:return@ForContent
                    val onIncrement = {
                        b1.add(IncrementEvent)
                    }
                    val onIncrement10 = {
                        b10.add(AdditionEvent(10))
                    }
                    val onIncrement20 = {
                        b20.add(AdditionEvent(20))
                    }
                    val onDecrement = {
                        b1.add(DecrementEvent)
                    }
                    val onDecrement10 = {
                        b10.add(SubtractionEvent(10))
                    }
                    val onDecrement20 = {
                        b20.add(SubtractionEvent(20))
                    }
                    //2nd template argument type (bloc state type) is inferred automatically
                    BlocBuilder<CounterBloc,_>("cnt1") { counterState->
                        CounterControls("Counter 1",
                            counterState.counter, onDecrement, onIncrement)
                    }
                    Divider(modifier = Modifier.height(2.dp))
                    //2nd template argument type (bloc state type)  is inferred automatically
                    BlocBuilder<CounterBloc,_>("cnt10") { counterState->
                        CounterControls("Counter 10",
                            counterState.counter, onDecrement10, onIncrement10)
                    }
                    Divider(modifier = Modifier.height(2.dp))
                    //2nd template argument type  (bloc state type) is inferred automatically
                    BlocBuilder<CounterBloc,_>("cnt20") { counterState->
                        CounterControls("Counter 20",
                            counterState.counter, onDecrement20, onIncrement20)
                    }

                }

        }
    }
}
