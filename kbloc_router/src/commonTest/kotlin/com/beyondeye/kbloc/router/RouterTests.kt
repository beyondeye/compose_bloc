package com.beyondeye.kbloc.router

import com.beyondeye.kbloc.router.utils.*
import com.beyondeye.kbloc.router.utils.Screen_bar
import com.beyondeye.kbloc.router.utils.Screen_foo
import com.beyondeye.kbloc.router.utils.Screen_nomatch
import com.beyondeye.kbloc.router.utils.Screen_with_string
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue



public class RouterTests {
    @Test
    public fun simplest() {
        val router = MockRouter()
        val routing = RoutingResolver("/") {
            route("foo") {
                Screen_foo(null)
            }
            route("bar") {
                Screen_bar(null)
            }
            noMatch {
                Screen_nomatch()
            }
        }
        router.navigate("/foo")
        val routed_foo = routing.resolveFor(router)
        assertEquals(Screen_foo(null), routed_foo)

        router.navigate("/bar")
        val routed_bar = routing.resolveFor(router)
        assertEquals(Screen_bar(null), routed_bar)

        router.navigate("/unrec")
        val routed_nomatch = routing.resolveFor(router)
        assertTrue { routed_nomatch is Screen_nomatch }
    }

    @Test
    public fun simple_noMatch() {
        val router = MockRouter()
        val routing = RoutingResolver("/") {
            route("foo") {
                noMatch {
                    Screen_foo(null)
                }
            }
            route("bar") {
                noMatch {
                    Screen_bar(null)
                }
            }
            noMatch {
                Screen_nomatch()
            }
        }
        router.navigate("/foo")
        val routed_foo = routing.resolveFor(router)
        assertEquals(Screen_foo(null), routed_foo)

        router.navigate("/bar")
        val routed_bar = routing.resolveFor(router)
        assertEquals(Screen_bar(null), routed_bar)

        router.navigate("/unrec")
        val routed_nomatch = routing.resolveFor(router)
        assertTrue { routed_nomatch is Screen_nomatch }
    }

    @Test
    public fun simple_with_param() {
        val router = MockRouter()
        val routing = RoutingResolver("/") {
            route("foo") {
                int {
                    Screen_foo(it)
                }
                noMatch {
                    Screen_foo(null)
                }
            }
            noMatch {
                Screen_nomatch()
            }
        }

        router.navigate("/foo")
        val routed_foo = routing.resolveFor(router)
        assertEquals(Screen_foo(null), routed_foo)

        router.navigate("/foo/1")
        val routed_foo_with_arg = routing.resolveFor(router)
        assertEquals(Screen_foo(1), routed_foo_with_arg)

        router.navigate("/unrecognized_root")
        val routed_nomatch = routing.resolveFor(router)
        assertTrue { routed_nomatch is Screen_nomatch }
    }

    @Test
    public fun blankRouteTest() {
        val router = MockRouter()
        val routing = RoutingResolver("/") {
            noMatch {
                var str = "other$remainingPath"
                val parameters = parameters
                if (parameters != null) {
                    str += parameters.raw
                }
                Screen_with_string(str)
            }
        }
        router.navigate("/")
        var routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("other/"), routed)

        router.navigate("/foo")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("other/foo"), routed)

        router.navigate("/foo", Parameters.from("V" to "b"))
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("other/fooV=b"), routed)
    }

    @Test
    public fun mixed() {
        val router = MockRouter()
        val routing = RoutingResolver("/") {
            int {
                Screen_with_string("int $it")
            }
            string {
                Screen_with_string("string $it")
            }
            noMatch {
                Screen_with_string("noMatch")
            }
        }

        var routed = routing.resolveFor(router) //default route
        assertEquals(Screen_with_string("noMatch"), routed)

        router.navigate("/42")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("int 42"), routed)

        router.navigate("/foo")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("string foo"), routed)
    }

    @Test
    public fun deepTest() {
        val router = MockRouter()
        val routing = RoutingResolver("/foo") {
            route("foo") {
                route("bar") {
                    route("baz") {
                        noMatch {
                            Screen_with_string("baz")
                        }
                    }
                    noMatch {
                        Screen_with_string("bar")
                    }
                }
                noMatch {
                    Screen_with_string("foo")
                }
            }
            noMatch {
                Screen_with_string("other")
            }
        }

        var routed = routing.resolveFor(router) //default route
        assertEquals(Screen_with_string("foo"), routed)

        router.navigate("/foo/bar")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("bar"), routed)

        router.navigate("/foo/bar/baz")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("baz"), routed)

        router.navigate("/")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("other"), routed)
    }

    @Test
    public fun nestedRoute() {
        //this must fails because we don't allow more than level of at
        // of route at once (route("foo/foo") is not allowed
        assertFailsWith<IllegalArgumentException> {
            val routing = RoutingResolver("/") {
                route("foo/foo") {
                    noMatch {
                        Screen_with_string("FooBar")
                    }
                }
                noMatch {
                    Screen_with_string("No match")
                }
            }
            val routed = routing.resolveFor(MockRouter())
        }
    }

    @Test
    public fun wrongDynamicTest() {
        val router = MockRouter()
        var addNewRoute = false
        val routing = RoutingResolver("/") {
            if (addNewRoute) {
                int {
                    Screen_with_string(it.toString())
                }
            }
            int {
                Screen_with_string("wrong")
            }
            noMatch {
                Screen_with_string("NoMatch")
            }
        }
        var routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("NoMatch"), routed)

        router.navigate("/1")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("wrong"), routed)

        addNewRoute = true
        router.navigate("/1")
        routed = routing.resolveFor(router)
        //here behavior is different from original code: only one path is matched, not multiple paths
        //here in the test the LAST matched path is returned. The more logical thing would be
        //to return the FIRST matched path
        // TODO think if it would be possible to change code to make it happen
        assertEquals(Screen_with_string("wrong"), routed)
    }

    @Test
    public fun correctDynamicTest() {
        val router = MockRouter()
        var addNewRoute = false
        val routing = RoutingResolver("/") {
            if (addNewRoute) {
                int {
                    Screen_with_string(it.toString())
                }
            } else {
                int {
                    Screen_with_string("correct")
                }
            }
            noMatch {
                Screen_with_string("NoMatch")
            }
        }
        var routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("NoMatch"), routed)

        router.navigate("/1")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("correct"), routed)

        addNewRoute = true
        router.navigate("/1")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("1"), routed)
    }

    //todo RouterCompositionLocal not relevant any more?
/*
    @Test
    fun RouterCompositionLocalCurrentRouterTest() = runTest {
        val mockRouter = MockRouter()
        var input: HTMLInputElement? = null
        composition {
            mockRouter("/") {
                route("foo") {
                    Text("Foo")
                }
                noMatch {
                    Text("NoMatch")

                    val router = Router.current
                    Input(type = InputType.Text) {
                        ref {
                            input = it
                            onDispose { }
                        }
                        onClick {
                            router.navigate(to = "/foo")
                        }
                    }
                }
            }
        }
        assertEquals("""NoMatch<input type="text">""", root.innerHTML)
        input!!.click()
        waitForRecompositionComplete()
        assertEquals("Foo", root.innerHTML)
    }
 */
//todo RouterCompositionLocal not relevant any more?
/*
    @Test
    fun RouterCompositionLocalRouterCurrentTest() = runTest {
        val mockRouter = MockRouter()
        var input: HTMLInputElement? = null
        composition {
            mockRouter("/") {
                route("foo") {
                    Text("Foo")
                }
                noMatch {
                    Text("NoMatch")

                    val router = Router.current
                    Input(type = InputType.Text) {
                        ref {
                            input = it
                            onDispose { }
                        }
                        onClick {
                            router.navigate(to = "/foo")
                        }
                    }
                }
            }
        }
        assertEquals("""NoMatch<input type="text">""", root.innerHTML)
        input!!.click()
        waitForRecompositionComplete()
        assertEquals("Foo", root.innerHTML)
    }
 */
//TODO relativeRouting could be interesting to implement? I don't know
/*
    @Test
    fun relativeRoutingTest() = runTest {
        var router: Router = MockRouter()
        composition {
            router.route("/") {
                route("foo") {
                    int {
                        uuid {
                            Text("UUID $it")
                            router = Router.current
                        }
                        noMatch {
                            Text("Int $it")
                            router = Router.current
                        }
                    }
                    noMatch {
                        Text("Foo")
                        router = Router.current
                    }
                }
                noMatch {
                    Text("NoMatch")
                    router = Router.current
                }
            }
        }
        assertEquals("NoMatch", root.innerHTML)

        router.navigate("/foo")
        waitForRecompositionComplete()
        assertEquals("Foo", root.innerHTML)

        router.navigate("42")
        waitForRecompositionComplete()
        assertEquals("Int 42", root.innerHTML)

        router.navigate(UUID.NIL.toString())
        waitForRecompositionComplete()
        assertEquals("UUID ${UUID.NIL}", root.innerHTML)

        router.navigate("/")
        waitForRecompositionComplete()
        assertEquals("NoMatch", root.innerHTML)
    }
 */
    @Test
    public fun relaxedTest() {
        val router = MockRouter()
        var addNewRoute = false
        val routing = RoutingResolver("foo") {
            route("/foo") {
                noMatch {
                    Screen_with_string("foo")
                }
            }
            route("bar") {
                noMatch {
                    Screen_with_string("bar")
                }
            }
            noMatch {
                Screen_with_string("other")
            }
        }
        var routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("foo"), routed)

        router.navigate("/bar")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("bar"), routed)

        router.navigate("/foo")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("foo"), routed)

        router.navigate("/asf")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("other"), routed)
    }
}

