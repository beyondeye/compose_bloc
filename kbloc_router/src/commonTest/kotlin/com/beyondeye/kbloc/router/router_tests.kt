package com.beyondeye.kbloc.router

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal data class Screen_foo(val arg:Int?):Screen {
    @Composable
    override fun Content() {
        TODO("Not yet implemented")
    }
}
internal data class Screen_bar(val arg:Int?):Screen {
    @Composable
    override fun Content() {
        TODO("Not yet implemented")
    }
}

internal class Screen_nomatch:Screen {
    @Composable
    override fun Content() {
        TODO("Not yet implemented")
    }
}
internal data class Screen_with_string(val str:String):Screen {
    @Composable
    override fun Content() {
        TODO("Not yet implemented")
    }
}

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
        val routed_foo= routing.resolveFor(router)
        assertEquals(Screen_foo(null),routed_foo)

        router.navigate("/bar")
        val routed_bar= routing.resolveFor(router)
        assertEquals(Screen_bar(null),routed_bar)

        router.navigate("/unrec")
        val routed_nomatch= routing.resolveFor(router)
        assertTrue { routed_nomatch is Screen_nomatch  }
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
        val routed_foo= routing.resolveFor(router)
        assertEquals(Screen_foo(null),routed_foo)

        router.navigate("/bar")
        val routed_bar= routing.resolveFor(router)
        assertEquals(Screen_bar(null),routed_bar)

        router.navigate("/unrec")
        val routed_nomatch= routing.resolveFor(router)
        assertTrue { routed_nomatch is Screen_nomatch  }
    }
    @Test
    public  fun simple_with_param() {
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
        val routed_foo= routing.resolveFor(router)
        assertEquals(Screen_foo(null),routed_foo)

        router.navigate("/foo/1")
        val routed_foo_with_arg= routing.resolveFor(router)
        assertEquals(Screen_foo(1),routed_foo_with_arg)

        router.navigate("/unrecognized_root")
        val routed_nomatch = routing.resolveFor(router)
        assertTrue { routed_nomatch is Screen_nomatch  }
    }
    @Test
    public fun blankRouteTest() {
        val router = MockRouter()
        val routing = RoutingResolver("/") {
            noMatch {
                var str="other$remainingPath"
                val parameters = parameters
                if (parameters != null) {
                    str += parameters.raw
                }
                Screen_with_string(str)
            }
        }
        router.navigate("/")
        var routed= routing.resolveFor(router)
        assertEquals(Screen_with_string("other/"), routed)

        router.navigate("/foo")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("other/foo"),routed)

        router.navigate("/foo",Parameters.from("V" to "b"))
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("other/fooV=b"),routed)
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

        var routed= routing.resolveFor(router) //default route
        assertEquals(Screen_with_string("noMatch"),routed)

        router.navigate("/42")
        routed= routing.resolveFor(router)
        assertEquals(Screen_with_string("int 42"), routed)

        router.navigate("/foo")
        routed= routing.resolveFor(router)
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

        var routed= routing.resolveFor(router) //default route
        assertEquals(Screen_with_string("foo"),routed)

        router.navigate("/foo/bar")
        routed= routing.resolveFor(router)
        assertEquals(Screen_with_string("bar"),routed)

        router.navigate("/foo/bar/baz")
        routed= routing.resolveFor(router)
        assertEquals(Screen_with_string("baz"),routed)

        router.navigate("/")
        routed= routing.resolveFor(router)
        assertEquals(Screen_with_string("other"),routed)
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
            val routed= routing.resolveFor(MockRouter())
        }
    }
    @Test
    public fun wrongDynamicTest() {
        val router = MockRouter()
        var addNewRoute =false
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
        assertEquals(Screen_with_string("NoMatch"),routed)

        router.navigate("/1")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("wrong"),routed)

        addNewRoute = true
        router.navigate("/1")
        routed = routing.resolveFor(router)
        //here behavior is different from original code: only one path is matched, not multiple paths
        //here in the test the LAST matched path is returned. The more logical thing would be
        //to return the FIRST matched path
        // TODO think if it would be possible to change code to make it happen
        assertEquals(Screen_with_string("wrong"),routed)
    }




}

