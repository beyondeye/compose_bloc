import cafe.adriel.voyager.navigator.renderComposableWithNavigator
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import screens.MainScreen

fun main() {
    Napier.base(DebugAntilog())
    renderComposableWithNavigator(screens = listOf(MainScreen()), rootElementId = "root")
}

