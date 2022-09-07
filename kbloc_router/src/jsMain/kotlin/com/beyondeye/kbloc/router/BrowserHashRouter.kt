package com.beyondeye.kbloc.router

//original code from https://github.com/hfhbd/routing-compose

import androidx.compose.runtime.*
import kotlinx.browser.*
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * `HashRouter` is used for hashed urls (e.g. yoursite.com/#/path).
 * This strategy requires no additional setup to work on a single page compose-web application.
 * Some SaaS providers, like GitHub Pages, do not offer configuration options, so you have to use `HashRouter`.
 *
 * This [Router] implementation uses `/#/path` to persistent the current route in [window.location.hash].
 *
 * Every request will always request `GET /`, so your server needs only to listen and serve this endpoint,
 * or using a SaaS `/index.html`.
 */
internal class BrowserHashRouter(private val currentHash:MutableStateFlow<String>) : RouterWithRawPathListener {
    init {
        currentHash.value= extractCurrentHashUrl() ?: ""
    }
    override val curPath: Path
        get() = Path.from(currentHash.value)

    //todo: should I return currentHash.value instead here?
    override fun getCurrentRawPath(initPath: String): String {
        return  extractCurrentHashUrl() ?: initPath
    }

    override fun setupRawPathListener(initPath: String) {
        currentHash.value = extractCurrentHashUrl() ?: initPath
        window.onhashchange = {
            currentHash.value = extractCurrentHashUrl() ?: ""
            Unit
        }
    }

    override fun removeRawPathListener() {
        window.onhashchange = null
    }

    private fun extractCurrentHashUrl() = window.location.hash.currentURL()
    private fun String.currentURL() = removePrefix("#")
        .removePrefix("/")
        .ifBlank { null }

    override fun navigate(to: String, hide: Boolean) {
        //TODO need to understand what hide do
        if (hide) {
            currentHash.value = to.currentURL() ?: ""
        } else if (extractCurrentHashUrl() == to.currentURL()) {
            currentHash.value = to.removePrefix("#")
        } else {
            window.location.hash = to
        }
    }

}
