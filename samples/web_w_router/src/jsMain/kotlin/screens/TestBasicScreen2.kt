package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.currentOrThrow
import com.beyondeye.kbloc.router.LocalNavigatorRouter
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

class TestBasicScreen2: Screen {
    @Composable
    override fun Content() {
        val router = LocalNavigatorRouter.currentOrThrow
        Div({ style { backgroundColor(screenColor); padding((25.px)) } }) {
            Text("this is screens.TestBasicScreen2")
        }
        Div {
            Button(attrs = { onClick { router.navigate(AppRoutes.home) } }) { Text("Click to go back") }
        }
    }
    companion object {
        val screenColor= Color.chartreuse
    }
}