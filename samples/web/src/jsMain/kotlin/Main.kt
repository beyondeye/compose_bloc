import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.renderComposableWithNavigator
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposableWithNavigator(screens = listOf(MainScreen()), rootElementId = "root")
}
class MainScreen:Screen {
    @Composable
    override fun Content() {
        val navigator= LocalNavigator.currentOrThrow
        Div({ style {backgroundColor(Test1BasicCounterBlocScreen.screenColor); padding(25.px) } }) {
            Button( attrs = { onClick { navigator.push(Test1BasicCounterBlocScreen())  }} ) {
                Text("Basic Counter Bloc")
        }}
        Div { }
        Div({ style {backgroundColor(Test2BasicCounterBlocScreen.screenColor);  padding(25.px) } }) {
            Button( attrs = { onClick { navigator.push(Test2BasicCounterBlocScreen())  }} ) {
                Text("Basic Counter Bloc 2")
            }}
    }
}

class Test1BasicCounterBlocScreen:Screen {
    @Composable
    override fun Content() {
        val navigator= LocalNavigator.currentOrThrow

        Div({style { backgroundColor(screenColor); padding((25.px))}}) {
            Text("this is Test1BasicCounterBlocScreen ")
        }
        Div() {
            Button(attrs = {onClick { navigator.pop()  }}) { Text("Click to go back") }
        }
    }
    companion object {
        val screenColor=Color.aliceblue
    }
}
class Test2BasicCounterBlocScreen:Screen {
    @Composable
    override fun Content() {
        val navigator= LocalNavigator.currentOrThrow
        Div({style { backgroundColor(screenColor); padding((25.px))}}) {
            Text("this is Test2BasicCounterBlocScreen ")
        }
        Div {
            Button(attrs = {onClick { navigator.pop()  }}) { Text("Click to go back") }
        }
    }
    companion object {
        val screenColor=Color.chartreuse
    }
}


fun main_old() {
    var count: Int by mutableStateOf(0)

    renderComposable(rootElementId = "root") {
        Div({ style { padding(25.px) } }) {
            Button(attrs = {
                onClick { count -= 1 }
            }) {
                Text("-")
            }

            Span({ style { padding(15.px) } }) {
                Text("$count")
            }

            Button(attrs = {
                onClick { count += 1 }
            }) {
                Text("+")
            }
        }
    }
}