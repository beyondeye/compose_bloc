package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.beyondeye.kbloc.compose.BlocBuilder
import com.beyondeye.kbloc.compose.BlocProvider
import com.beyondeye.kbloc.compose.rememberProvidedBlocOf
import io.github.aakira.napier.Napier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text


class TestBasicCounterCubitScreenWeb : Screen {
    @Composable
    override fun Content() {
        TestScreenContent()
    }

    @Composable
    private fun TestScreenContent() {
        val navigator = LocalNavigator.currentOrThrow
        Napier.d("recomposition of TestScreenContent")
        BlocProvider(create = { cscope -> CounterCubit(cscope, 1) }) {
            //rememberProvidedBlocOf is similar to dependency injection: it retrieves the specified
            //bloc type as defined by the closest enclosing BlocProvider

            val b = rememberProvidedBlocOf<CounterCubit>() ?: return@BlocProvider
            Napier.d("recomposition of BlocProvider bloc $b")
            val onIncrement = { b.increment() }
            val onDecrement = { b.decrement() }

            //BlocBuilder search for the specified bloc type as defined by the closest enclosing
            //blocProvider and subscribes to its states updates, as a Composable state that
            //when changes trigger recomposition
            BlocBuilder(b) { counterState ->
                GlobalScope.async {
                    Napier.d("going to read state of ${b.state}")
//                    Napier.d("state value:${counterState}")
                }
                //TODO: for some reason reading counterState generate an uncaught runtime exception
                //with a compose runtime internal error. It do not make any difference if we try to
                //read counterState in blocBuilder body or in a global coroutine as in the line above
                // but using instead b.state.counter directly seems to be working
                // what is strange that reading it (the exact same variable) inside BlocBuilder method
                // code works fine.
                // Also: this is happening in Javascript but not on Android (not that on Javascript
                //  we are using Jetbrain compose fork while on Android the official Jetpack Compose
                CounterControls_web(
                    description,
                    b.state.counter,//counterState.counter, TODO: I should use counterState.counter here but compose internal error
                    onDecrement, onIncrement
                )

            }
        }
        Div {
            Button(attrs = { onClick { navigator.pop() } }) { Text("Click to go back") }
        }
    }

    companion object {
        val screenColor = Color.orangered
        val description="Basic Counter Cubit Screen"
    }
}
