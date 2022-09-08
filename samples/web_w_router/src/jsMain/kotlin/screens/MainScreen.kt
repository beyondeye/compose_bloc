package screens

import AppRoutes
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.currentOrThrow
import com.beyondeye.kbloc.router.LocalNavigatorRouter
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

class MainScreen: Screen {
    @Composable
    override fun Content() {
        val router = LocalNavigatorRouter.currentOrThrow
        Div({ style { backgroundColor(TestBasicScreen1.screenColor); padding(25.px) } }) {
            Button(attrs = { onClick { router.navigate("/${AppRoutes.testbasic_(1)}") } }) {
                Text("Basic Screen 1")
            }
        }
        Div { }
        Div({ style { backgroundColor(TestBasicScreen2.screenColor); padding(25.px) } }) {
            Button(attrs = { onClick { router.navigate("/${AppRoutes.testbasic_(2)}") } }) {
                Text("Basic Screen 2")
            }
        }
        Div { }
        Div({ style { backgroundColor(TestBasicCounterBlocScreenWeb.screenColor); padding(25.px) } }) {
            Button(attrs = { onClick { router.navigate("/${AppRoutes.counter_bloc}") } }) {
                Text(TestBasicCounterBlocScreenWeb.description)
            }
        }
        Div { }
        Div({ style { backgroundColor(TestBasicCounterCubitScreenWeb.screenColor); padding(25.px) } }) {
            Button(attrs = { onClick { router.navigate("/${AppRoutes.counter_cubit}") } }) {
                Text(TestBasicCounterCubitScreenWeb.description)
            }
        }

    }
}