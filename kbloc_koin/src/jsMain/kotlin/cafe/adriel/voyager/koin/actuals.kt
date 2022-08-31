package cafe.adriel.voyager.koin

import org.koin.core.context.GlobalContext
import org.koin.core.context.KoinContext

// https://github.com/InsertKoinIO/koin/blob/main/core/koin-core/src/jsMain/kotlin/org/koin/core/context/GlobalContext.kt
actual internal fun getAppKoinContext(): KoinContext {
    return GlobalContext
}
