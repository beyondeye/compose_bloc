package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.beyondeye.kbloc.compose.BlocBuilder
import com.beyondeye.kbloc.compose.BlocProvider
import com.beyondeye.kbloc.compose.rememberProvidedBlocOf
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text


class TestBasicCounterBlocScreenWeb : Screen {
    @Composable
    override fun Content() {
        Test1ScreenContent()
    }

    @Composable
    private fun Test1ScreenContent() {
        val navigator = LocalNavigator.currentOrThrow
        BlocProvider(create = { cscope -> CounterBloc(cscope, 1) }) {
            //rememberProvidedBlocOf is similar to dependency injection: it retrieves the specified
            //bloc type as defined by the closest enclosing BlocProvider
            val b = rememberProvidedBlocOf<CounterBloc>() ?: return@BlocProvider
            val onIncrement = { b.add(IncrementEvent) }
            val onDecrement = { b.add(DecrementEvent) }
            //BlocBuilder search for the specified bloc type as defined by the closest enclosing
            //blocProvider and subscribes to its states updates, as a Composable state that
            //when changes trigger recomposition
            BlocBuilder(b){ counterState ->
                CounterControls_web(
                    "Counter display updated always",
                    counterState.counter,
                    onDecrement, onIncrement
                )
                Div() {
                    Button(attrs = {onClick { navigator.pop()  }}) { Text("Click to go back") }
                }
            }
        }
    }
    companion object {
        val screenColor= Color.rebeccapurple
    }
}

@Composable
fun CounterControls_web(
    explanatoryText:String,
    counterValue: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Div({ style { padding(25.px) } }) {
        Text(explanatoryText)
    }
    Div({ style { padding(25.px) } }) {
        Text("Counter value: ${counterValue}")
    }
    Div({ style { padding(25.px) } }) {
        Button(attrs = {
            onClick { onDecrement() }
        }) {
            Text("-")
        }
        Button(attrs = {
            onClick { onIncrement() }
        }) {
            Text("+")
        }
    }
}