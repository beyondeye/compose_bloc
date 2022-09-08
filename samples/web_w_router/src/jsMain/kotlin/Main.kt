import com.beyondeye.kbloc.router.renderComposableInBodyWithNavigator
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier


fun main() {
    Napier.base(DebugAntilog())
    renderComposableInBodyWithNavigator(app_routing)
}

