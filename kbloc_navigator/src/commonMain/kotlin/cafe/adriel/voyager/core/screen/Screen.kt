package cafe.adriel.voyager.core.screen

import androidx.compose.runtime.Composable
import java.io.Serializable

public interface Screen: Serializable {

    public val key: ScreenKey get() =commonKeyGeneration()
    @Composable
    public fun Content()
}

internal fun Screen.commonKeyGeneration() =
    this::class.qualifiedName ?: error("Default ScreenKey not found, please provide your own key")
