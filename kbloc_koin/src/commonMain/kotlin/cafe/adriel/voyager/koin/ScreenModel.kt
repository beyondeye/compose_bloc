package cafe.adriel.voyager.koin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.koin.core.Koin
import org.koin.core.context.KoinContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

@PublishedApi
expect internal fun getAppKoinContext(): KoinContext

@PublishedApi
@Composable
internal fun getKoin(): Koin = remember {
    getAppKoinContext().get()
}

@Composable
public inline fun <reified T : ScreenModel> Screen.getScreenModel(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T {
    val koin = getKoin()
    return rememberScreenModel(tag = qualifier?.value) { koin.get(qualifier, parameters) }
}
