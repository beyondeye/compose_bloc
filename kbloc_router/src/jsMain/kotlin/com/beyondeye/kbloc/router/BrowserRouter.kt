package com.beyondeye.kbloc.router

//original code from https://github.com/hfhbd/routing-compose

import androidx.compose.runtime.*
import kotlinx.browser.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.*

/**
* BrowserRouter` is used for traditional urls (e.g. yoursite.com/path).
 *Using this strategy will require additional work on a single page compose-web application, requiring you to implement a catch-all strategy to return the same html resource for all paths.
 *This strategy will be different for different server configurations.

 * A router leveraging the History API (https://developer.mozilla.org/en-US/docs/Web/API/History).
 *
 * Using a BrowserRouter requires you to implement a catch-all to send the same resource for
 * every path the router intends to control.
 * BrowserRouter will handle the proper child composition.
 *
 * Without a catch-all rule, will get a 404 or "Cannot GET /path" error each time you refresh or
 * request a specific path.
 * Each server's implementation of a catch-all will be different, and you
 * should handle this based on the webserver environment you're running.
 *
 * For more information about this catch-all, check your webserver implementation's specific
 * instructions.
#### Development usage:

The `browser` target for Kotlin/JS uses [webpack-dev-server](https://github.com/webpack/webpack-dev-server) as a local development server.
We need our webpack config to serve `index.html` (or your primary html file) for all paths to work with `BrowserRouter`.
This is done in webpack-dev-server through the webpack config's [devServer.historyApiFallback](https://webpack.js.org/configuration/dev-server/#devserverhistoryapifallback) flag.

The Kotlin webpack DSL currently [does not support the `historyApiFallback` flag](https://github.com/JetBrains/kotlin/blob/master/libraries/tools/kotlin-gradle-plugin/src/main/kotlin/org/jetbrains/kotlin/gradle/targets/js/webpack/KotlinWebpackConfig.kt#L165), but we can add it through additional [webpack configuration files](https://kotlinlang.org/docs/js-project-setup.html#webpack-configuration-file) that will be merged with the auto-generated `webpack.config.js` when building.

##### Instructions

First, create a directory in the top-most project directory named `webpack.config.d`.

Create a new `.js` file containing a `config.devServer` configuration setting `historyApiFallback = true`.
You can name this file any name you wish, it will be merged into the project's main `webpack.config.js`.

```javascript
// YourProject/webpack.config.d/devServerConfig.js

config.devServer = {
...config.devServer, // Merge with other devServer settings
"historyApiFallback": true
};
```

Then run your web app, and it should route all paths to a valid route.
You can confirm this by refreshing or manually entering a path.


 */
internal class BrowserRouter(private val currentLocation: MutableStateFlow<String>) : RouterWithRawPathListener {
    init {
        currentLocation.value= extractCurrentLocationUrl() ?: ""
    }
    override val curPath: Path
        get() = Path.from(currentLocation.value)


    //todo should I return currentLocation.value instead here?
    override fun getCurrentRawPath(initPath: String): String {
        return extractCurrentLocationUrl().takeUnless { it == "/" } ?: initPath
    }

    @Composable
    override fun setupRawPathListener(initPath: String) {
        currentLocation.value = extractCurrentLocationUrl().takeUnless { it == "/" } ?: initPath
        window.onpopstate = {
            currentLocation.value = extractCurrentLocationUrl()
            Unit
        }
    }

    override fun removeRawPathListener() {
        window.onpopstate=null
    }

    private fun extractCurrentLocationUrl():String {
        return window.location.newPath()
    }
    private fun Location.newPath() = "$pathname$search"

    override fun navigate(to: String, hide: Boolean) {
        if (hide) {
            currentLocation.value = to
        } else {
            window.history.pushState(null, "", to)
            /*
                The history API unfortunately provides no callback to listen to
                [window.history.pushState], so we need to notify subscribers when pushing a new path.
                */
            currentLocation.value = extractCurrentLocationUrl()
        }
    }
}
