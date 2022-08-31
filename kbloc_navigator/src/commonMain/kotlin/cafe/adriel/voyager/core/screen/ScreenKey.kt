package cafe.adriel.voyager.core.screen

import kotlinx.atomicfu.atomic


public typealias ScreenKey = String

private val nextScreenKey = atomic(0)

public val Screen.uniqueScreenKey: ScreenKey
    get() = "Scr#${nextScreenKey.getAndIncrement()}"
