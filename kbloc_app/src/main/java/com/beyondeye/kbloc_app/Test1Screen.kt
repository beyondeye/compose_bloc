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

class CounterBloc(cscope:CoroutineScope): Bloc<CounterEvent,CounterState>(cscope,
    CounterState(0),false
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
            /*
            BlocProvider(create = {cscope-> CounterBloc(cscope)} ) {
                val b= rememberProvidedBlocOf<CounterBloc>()
                BlocBuilder<CounterBloc,CounterState>() { counterState->
                    Text("Counter value: ${counterState.counter}")
                    Button(onClick = { b?.add(IncrementEvent) }) { Text(text="Increment") }
                    Button(onClick = { b?.add(DecrementEvent) }) { Text(text="Decrement") }
                }
            }
             */
        }
    }
}