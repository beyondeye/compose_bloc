package com.beyondeye.kbloc.router

//original code from https://github.com/hfhbd/routing-compose
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen

/**
 * Use the DSL functions to build the expected route handled by a [Router].
 * If two routes match the same path, the first declared route is chosen.
 *
 * With dynamic routing displaying will not stop if two routes of the same kind match the current route:
 *
 * wrong usage:
 * ```kotlin
 *     if (true) {
 *       int {
 *         Text("Match")
 *       }
 *     }
 *     int {
 *       Text("Will be displayed too")
 *     }
 * ```
 * correct usage:
 * ```kotlin
 *     if (true) {
 *       int {
 *         Text("Match")
 *       }
 *     } else {
 *       int {
 *         Text("Won't be displayed")
 *       }
 *     }
 * ```
 */
@Routing
public class RouteBuilder internal constructor(
    private val router:Router,
    /**
     * initially this the path without the query parameters
     */
    private val basePath: String,
    /**
     * initially the path object that also contain information of the query parameters
     * but when [RootBuilder.execute] is run this is going to change: todo: document what is going to happen
     */
    private val remainingPath: Path
) {
    public val parameters: Parameters? = remainingPath.parameters

    private var match =Match.NoMatch
    private var match_res:Screen? = null

    private enum class Match {
        Constant, Integer, String, Uuid, NoMatch
    }

    /**
     * Executes its children when the requested subroute matches one of these constant [route].
     *
     * To match `foo/bar`, create a [route] inside the first [route].
     * *DARIO* note that although the argument for the method is vararg, in the
     * code only a single string argument is used for specifying a single level if routing
     * the use of vararg is for assigning the same route to multiple "subpaths" at the same time
     * that should resolve to the same route
     */
    @Routing
    public fun route(
        vararg route: String,
        /**
         * *DARIO* this is potentially a nested route but usually is simply the specification
         * of the content of this route depending of the value of the matched route parameters
         *
         */
        nestedRoute: RouteBuilder.() -> Screen?
    ) {
        val relaxedRoute = route.check()
        val currentPath = remainingPath.currentPath
        //*DARIO* currentPath is the path that we need to match with this route
        //*DARIO* Match.Constant means a match with no parameters
        if ((match == Match.NoMatch || match == Match.Constant) && currentPath in relaxedRoute) {
            execute(currentPath, nestedRoute)
            match = Match.Constant
        }
    }

    /**
     * *DARIO* remove "/" prefix and or suffix from an array of strings (but in the code
     * used only for an array of size 1) and also validate that there is no remaining "/"
     * inside so the string is a actually a single level in the specification of a "route"
     */
    private fun Array<out String>.check(): List<String> {
        val relaxedRoute = map { it.removePrefix("/").removeSuffix("/") }
        require(relaxedRoute.none { it.contains("/") }) { "To use nested routes, use route() { route() { } } instead." }
        return relaxedRoute
    }

    @Routing
    public fun redirect(vararg route: String, target: String, hide: Boolean = false) {
        val routes = route.check()
        val currentPath = remainingPath.currentPath
        if (match == Match.NoMatch && currentPath in routes) {
            match= Match.Constant
            match_res=__Redirect //we will rerun routing again
            router.navigate(target, hide)
        }
    }

    private fun execute(currentPath: String, nestedRoute: RouteBuilder.() -> Screen?) {
        val newPath = remainingPath.newPath(currentPath)
        //we matched one level of the route so now we create a new router that refer to
        // the parent router
        val delegatingRouter = DelegateRouter(basePath, router)
        val newState = RouteBuilder(delegatingRouter, basePath, newPath)
        match_res=newState.nestedRoute()
    }

    /**
     * Executes its children when the requested subroute is a non-empty [String].
     */
    @Routing
    public fun string(nestedRoute: RouteBuilder.(String) -> Screen):Screen? {
        val currentPath = remainingPath.currentPath
        if ((match == Match.NoMatch || match == Match.String) && currentPath.isNotEmpty()) {
            execute(currentPath) {
                nestedRoute(currentPath)
            }
            match = Match.String
        }
        return match_res
    }

    /**
     * Executes its children when the requested subroute is a [Int].
     */
    @Routing
    public fun int(nestedRoute: RouteBuilder.(Int) -> Screen):Screen? {
        val currentPath = remainingPath.currentPath
        val int = currentPath.toIntOrNull()
        if ((match == Match.NoMatch || match == Match.Integer) && int != null) {
            execute(currentPath) {
                nestedRoute(int)
            }
            match = Match.Integer
        }
        return match_res
    }


    /**
     * Fallback if no matching route is found.
     */
    @Routing
    public fun noMatch(content: NoMatch.() -> Screen):Screen? {
        if (match == Match.NoMatch) {
            match_res=NoMatch(this,remainingPath.path, remainingPath.parameters).content()
        }
        return match_res
    }

    @Routing
    public class NoMatch(
        private val rb: RouteBuilder,
        public val remainingPath: String,
        public val parameters: Parameters?
    ) {
        @Routing
        public fun redirect(target: String, hide: Boolean = false):Screen? {
            rb.match= Match.Constant
            rb.match_res=__Redirect //we will rerun routing again
            rb.router.navigate(target, hide)
            return __Redirect
        }
    }
}
