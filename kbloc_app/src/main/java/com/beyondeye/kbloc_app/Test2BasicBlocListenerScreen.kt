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
import com.beyondeye.kbloc.compose.bloc.rememberProvidedBlocOf
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.core.LOGTAG


class Test2BasicBlocListenerScreen: Screen {
    @Composable
    override fun Content() {
        Test2ScreenContent()
    }

    @Composable
    private fun Test2ScreenContent() {
        Column(modifier=Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            // out of the BlocProvider composable subtree the bloc is not available
            val bnull= rememberProvidedBlocOf<CounterBloc>()
            Log.e(LOGTAG,"obtained bnull counter bloc: $bnull")  //this must be null
            //BlocProvider makes available the specified bloc (CounterBloc) to associated the composable subtree
            BlocProvider(create = {cscope-> CounterBloc(cscope,1)} ) {
                //rememberProvidedBlocOf is similar to dependency injection: it retrieves the specified
                //bloc type as defined by closest enclosing BlocProvider
                val b= rememberProvidedBlocOf<CounterBloc>()?:return@BlocProvider
                Log.e(LOGTAG,"obtained counter bloc: $b, with count ${b.state.counter}")
                val onIncrement = { b.add(IncrementEvent) }
                val onDecrement = { b.add(DecrementEvent) }
                //BlocBuilder search of the specified bloc type as defined by the closest enclosing
                //bloc provider and subscribe to its states update as Composable state that
                //when changes trigger recomposition
                BlocBuilder<CounterBloc,CounterState>() { counterState->
                    CounterControls("Counter display updated always",counterState, onDecrement, onIncrement)
                }
                Divider(modifier = Modifier.height(2.dp))
                //the buildWhen condition here causes update of counterState value  when counter is even
                BlocBuilder<CounterBloc,CounterState>(
                    buildWhen = {prev,cur -> cur.counter%2==0 }) { onlyEvenCounterState->
                    CounterControls("Counter display updated only for even values",onlyEvenCounterState, onDecrement, onIncrement)
                }
            }
            // out of the BlocProvider composable subtree the bloc is not available
            val bnull2= rememberProvidedBlocOf<CounterBloc>()
            Log.e(LOGTAG,"obtained bnull2 counter bloc: $bnull2") //this must be null

        }
    }
}

@Composable
private fun CounterControls(
    explanatoryText:String,
    counterState: CounterState,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Text(explanatoryText)
    Text("Counter value: ${counterState.counter}")
    Row {
        Button(
            onClick = onDecrement,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) { Text(text = "-") }
        Button(
            onClick = onIncrement,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) { Text(text = "+") }
    }
}