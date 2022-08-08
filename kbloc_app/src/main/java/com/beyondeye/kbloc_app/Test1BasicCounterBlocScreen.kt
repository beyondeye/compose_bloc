package com.beyondeye.kbloc_app

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beyondeye.kbloc.compose.bloc.BlocBuilder
import com.beyondeye.kbloc.compose.bloc.BlocProvider
import com.beyondeye.kbloc.compose.bloc.rememberProvidedBlocOf
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.core.Bloc
import com.beyondeye.kbloc.core.LOGTAG
import com.beyondeye.kbloc_app.ui.theme.Compose_blocTheme
import kotlinx.coroutines.CoroutineScope

interface CounterEvent
object IncrementEvent:CounterEvent
object DecrementEvent:CounterEvent
data class CounterState(val counter:Int=0)

class CounterBloc(cscope:CoroutineScope, startCounter:Int=0): Bloc<CounterEvent,CounterState>(cscope,
    CounterState(startCounter),false
) {
    init {
        on<IncrementEvent> { event, emit ->
            val s=state
            emit(s.copy(counter =s.counter+1 ))
        }
        on<DecrementEvent> { event, emit ->
            val s=state
            emit(s.copy(counter =s.counter-1 ))
        }
    }

}

class Test1BasicCounterBlocScreen: Screen {
    @Composable
    override fun Content() {
        Test1ScreenContent()
    }

    @Composable
    private fun Test1ScreenContent() {
        Column(modifier=Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            BlocProvider(create = {cscope-> CounterBloc(cscope,1)} ) {
                val b= rememberProvidedBlocOf<CounterBloc>()
                Log.e(LOGTAG,"obtained counter bloc: $b, with count ${b?.state?.counter}")
                BlocBuilder<CounterBloc,CounterState>() { counterState->
                    Text("Counter value: ${counterState.counter}")
                    Row {
                        Button(
                            onClick = { b?.add(DecrementEvent) },
                            modifier = Modifier.padding(horizontal = 8.dp)) { Text(text="-") }
                        Button(
                            onClick = { b?.add(IncrementEvent) },
                            modifier = Modifier.padding(horizontal = 8.dp)) { Text(text="+") }
                    }
                }
            }
        }
    }
}