package com.beyondeye.kbloc_app

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
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

class Test1Screen: Screen {
    @Composable
    override fun Content() {
        Test1ScreenContent()
    }

    @Composable
    private fun Test1ScreenContent() {
        Column(modifier=Modifier.fillMaxWidth()) {
            Text(text="Test1")
            Text(text="Test2")
            BlocProvider(create = {cscope-> CounterBloc(cscope,1)} ) {
                val b= rememberProvidedBlocOf<CounterBloc>()
                //there is a deadlock when waiting to obtain the current value of b?.state?.counter
                //TODO perhaps because this is run on the main thread, but this should not cause a deadlock:
                //what it is happening?
                //need to change the DISPATCHER used to be multithreaded and not main for processing
                // bloc event etc?? need to check
//                Log.e(LOGTAG,"obtained counter bloc: $b, with count ${b?.state?.counter}")
                Log.e(LOGTAG,"obtained counter bloc: $b")
                /*
                BlocBuilder<CounterBloc,CounterState>() { counterState->
                    Text("Counter value: ${counterState.counter}")
                    Button(onClick = { b?.add(IncrementEvent) }) { Text(text="Increment") }
                    Button(onClick = { b?.add(DecrementEvent) }) { Text(text="Decrement") }
                }
                 */
            }
        }
    }
}