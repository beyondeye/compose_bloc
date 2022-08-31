package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

class MainScreen: Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        Div({ style { backgroundColor(TestBasicScreen1.screenColor); padding(25.px) } }) {
            Button(attrs = { onClick { navigator.push(TestBasicScreen1()) } }) {
                Text("Basic Screen 1")
            }
        }
        Div { }
        Div({ style { backgroundColor(TestBasicScreen2.screenColor); padding(25.px) } }) {
            Button(attrs = { onClick { navigator.push(TestBasicScreen2()) } }) {
                Text("Basic Screen 2")
            }
        }
        Div { }
        Div({ style { backgroundColor(TestBasicCounterBlocScreenWeb.screenColor); padding(25.px) } }) {
            Button(attrs = { onClick { navigator.push(TestBasicCounterBlocScreenWeb()) } }) {
                Text("Basic Counter Bloc Screen")
            }
        }
    }
}