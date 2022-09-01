package screens

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun CounterControls_web(
    explanatoryText: String,
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