package com.beyondeye.kbloc.router

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import kotlin.test.Test
import kotlin.test.assertEquals
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
    public fun explicitely_access_params_and_remaniningpath() {
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
}

