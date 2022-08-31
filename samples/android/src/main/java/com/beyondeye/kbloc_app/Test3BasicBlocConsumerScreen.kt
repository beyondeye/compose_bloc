package com.beyondeye.kbloc_app

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.beyondeye.kbloc.compose.BlocConsumer
import com.beyondeye.kbloc.compose.BlocProvider
import com.beyondeye.kbloc.compose.rememberProvidedBlocOf
import com.beyondeye.kbloc.core.LOGTAG


class Test3BasicBlocConsumerScreen: Screen {
    @Composable
    override fun Content() {
        Test3ScreenContent()
    }

    @Composable
    private fun Test3ScreenContent() {
        Column(modifier=Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            //BlocProvider makes available the specified bloc (CounterBloc) to the associated  composable subtree
            BlocProvider(create = {cscope-> CounterBloc(cscope,1)} ) {
                //rememberProvidedBlocOf is similar to dependency injection: it retrieves the specified
                //bloc type as defined by closest enclosing BlocProvider
                val b= rememberProvidedBlocOf<CounterBloc>()?:return@BlocProvider
                Log.e(LOGTAG,"obtained counter bloc: $b, with count ${b.state.counter}")
                val onIncrement = { b.add(IncrementEvent) }
                val onDecrement = { b.add(DecrementEvent) }

                val listener:(CounterState)->Unit= {counterState->
                    Log.e(LOGTAG, "listener1 triggered with count ${counterState.counter} ")
                }
                //BlocConsumer search for the specified bloc type as defined by the closest enclosing
                //blocProvider and subscribes to its states updates, as a Composable state that
                //when changes trigger recomposition and also add a callback function triggered by state updates
                //it is basically a BlocBuilder and BlocListener combined
                //2nd template argument type  (bloc state type) is inferred automatically
                BlocConsumer<CounterBloc,_>(listener=listener,
                    listenWhen = { prev, cur -> cur.counter % 2 == 0 },
                    buildWhen = {prev, cur -> cur.counter % 1 == 0}) { counterState ->
                    CounterControls(
                        "Counter display updated always",
                        counterState.counter,
                        onDecrement, onIncrement
                    )
                }
            }
        }
    }
}
