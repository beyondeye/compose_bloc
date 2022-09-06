package com.beyondeye.kbloc.router

//original code from https://github.com/hfhbd/routing-compose

import androidx.compose.runtime.*
import kotlinx.browser.*

/**
 * This [Router] implementation uses `/#/path` to persistent the current route in [window.location.hash].
 *
 * Every request will always request `GET /`, so your server needs only to listen and serve this endpoint,
 * or using a SaaS `/index.html`.
 */
@Composable
public fun HashRouter(
    initPath: String,
    routingDefBuilder: @Composable RoutingDefBuilder.() -> Unit
) {
    HashRouter().route(initPath, routingDefBuilder)
}

internal class HashRouter : Router {
    override val currentPath: Path
        get() = Path.from(currentHash.value)

    private val currentHash: MutableState<String> = mutableStateOf(window.location.hash.currentURL() ?: "")

    @Composable
    override fun getCurrentRawPath(initPath: String): State<String> {
        LaunchedEffect(Unit) {
            currentHash.value = window.location.hash.currentURL() ?: initPath
            window.onhashchange = {
                currentHash.value = window.location.hash.currentURL() ?: ""
                Unit
            }
        }
        return currentHash
    }

    private fun String.currentURL() = removePrefix("#")
        .removePrefix("/")
        .ifBlank { null }

    override fun navigate(to: String, hide: Boolean) {
        if (hide) {
            currentHash.value = to.currentURL() ?: ""
        } else if (window.location.hash.currentURL() == to.currentURL()) {
            currentHash.value = to.removePrefix("#")
        } else {
            window.location.hash = to
        }
    }
}