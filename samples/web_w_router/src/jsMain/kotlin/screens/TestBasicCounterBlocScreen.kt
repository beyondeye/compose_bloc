package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.currentOrThrow
import com.beyondeye.kbloc.compose.BlocBuilder
import com.beyondeye.kbloc.compose.BlocProvider
import com.beyondeye.kbloc.compose.rememberProvidedBlocOf
import com.beyondeye.kbloc.router.LocalNavigatorRouter
import io.github.aakira.napier.Napier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text


class TestBasicCounterBlocScreenWeb : Screen {
    @Composable
    override fun Content() {
        TestScreenContent()
    }

    @Composable
    private fun TestScreenContent() {
        val router = LocalNavigatorRouter.currentOrThrow
        BlocProvider(create = { cscope -> CounterBloc(cscope, 1) }) {
            //rememberProvidedBlocOf is similar to dependency injection: it retrieves the specified
            //bloc type as defined by the closest enclosing BlocProvider
            val b = rememberProvidedBlocOf<CounterBloc>() ?: return@BlocProvider
            val onIncrement = { b.add(IncrementEvent) }
            val onDecrement = { b.add(DecrementEvent) }

            //BlocBuilder search for the specified bloc type as defined by the closest enclosing
            //blocProvider and subscribes to its states updates, as a Composable state that
            //when changes trigger recomposition
            BlocBuilder(b) { counterState ->
                GlobalScope.async {
                    Napier.d("going to read state of ${b.state}")
//                    Napier.d("state value:${counterState.counter}")
                }
                //TODO: for some reason reading counterState generate an uncaught runtime exception
                //with a compose runtime internal error. It do not make any difference if we try to
                //read counterState in blocBuilder body or in a global coroutine as in the line above
                // but using instead b.state.counter directly seems to be working
                CounterControls_web(
                    description,
                    b.state.counter,//counterState.counter, TODO: I should use counterState.counter here but compose internal error
                    onDecrement, onIncrement
                )

            }
        }
        Div {
            Button(attrs = { onClick { router.navigate(AppRoutes.home) } }) { Text("Click to go back") }
        }
    }

    companion object {
        val screenColor = Color.rebeccapurple
        val description="Basic Counter Bloc Screen"
    }
}

