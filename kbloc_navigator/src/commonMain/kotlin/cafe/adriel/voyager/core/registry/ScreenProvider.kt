package cafe.adriel.voyager.core.registry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen

/**
 * see https://voyager.adriel.cafe/navigation/multi-module-navigation
 */
@Composable
public inline fun <reified T : ScreenProvider> rememberScreen(provider: T): Screen =
    remember(provider) {
        ScreenRegistry.get(provider)
    }

public interface ScreenProvider
