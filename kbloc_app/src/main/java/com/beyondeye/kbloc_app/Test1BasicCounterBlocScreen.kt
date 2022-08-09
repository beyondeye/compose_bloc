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
import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.LOGTAG
import kotlinx.coroutines.CoroutineScope



class Test1BasicCounterBlocScreen: Screen {
    @Composable
    override fun Content() {
        Test1ScreenContent()
    }

    @Composable
    private fun Test1ScreenContent() {
        Column(modifier=Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            // out of the BlocProvider composable subtree the bloc is not available
            val bnull= rememberProvidedBlocOf<CounterBloc>()
            Log.e(LOGTAG,"obtained bnull counter bloc: $bnull")  //this must be null
            //BlocProvider makes available the specified bloc (CounterBloc) to the associated composable subtree
            BlocProvider(create = {cscope-> CounterBloc(cscope,1)} ) {
                //rememberProvidedBlocOf is similar to dependency injection: it retrieves the specified
                //bloc type as defined by the closest enclosing BlocProvider
                val b= rememberProvidedBlocOf<CounterBloc>()?:return@BlocProvider
                Log.e(LOGTAG,"obtained counter bloc: $b, with count ${b.state.counter}")
                val onIncrement = { b.add(IncrementEvent) }
                val onDecrement = { b.add(DecrementEvent) }
                //BlocBuilder search for the specified bloc type as defined by the closest enclosing
                //blocProvider and subscribes to its states updates, as a Composable state that
                //when changes trigger recomposition
                //2nd template argument type  (bloc state type) is inferred automatically
                BlocBuilder<CounterBloc,_> { counterState->
                    CounterControls(
                        "Counter display updated always",
                        counterState.counter,
                        onDecrement, onIncrement)
                }
                Divider(modifier = Modifier.height(2.dp))
                //the buildWhen condition here filter updates of onlyEvenCounterState value only  when counter is even
                //2nd template argument type  (bloc state type) is inferred automatically
                BlocBuilder<CounterBloc,_>(
                    buildWhen = {prev,cur -> cur.counter%2==0 }) { onlyEvenCounterState->
                    CounterControls(
                        "Counter display updated only for even values",
                        onlyEvenCounterState.counter,
                        onDecrement, onIncrement)
                }
            }
            // out of the BlocProvider composable subtree the bloc is not available
            val bnull2= rememberProvidedBlocOf<CounterBloc>()
            Log.e(LOGTAG,"obtained bnull2 counter bloc: $bnull2") //this must be null

        }
    }
}

@Composable
fun CounterControls(
    explanatoryText:String,
    counterValue: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Text(explanatoryText)
    Text("Counter value: ${counterValue}")
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