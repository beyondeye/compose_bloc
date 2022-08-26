package cafe.adriel.voyager.core.screen

import androidx.compose.runtime.Composable
import com.beyondeye.kbloc.ext.getFullName

public typealias ScreenKey = String

// the only reason why Screen is an expect interface is because in JVM it is defined as Serializable
// cannot define a class as Serializable in common code
public expect interface Screen {
    public open val key: ScreenKey
    @Composable
    public fun Content()
}

internal fun Screen.commonKeyGeneration() =
    this::class.getFullName()
