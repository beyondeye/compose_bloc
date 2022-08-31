import cafe.adriel.voyager.navigator.renderComposableWithNavigator
import screens.MainScreen

fun main() {
    renderComposableWithNavigator(screens = listOf(MainScreen()), rootElementId = "root")
}

