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

internal class RouterTests {
    @Test
    fun simplest() {
        val router = MockRouter()
        val routing = RoutingResolver {
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
        val routed_foo= routing(router,"/foo")
        assertEquals(Screen_foo(null),routed_foo)
        val routed_bar= routing(router,"/bar")
        assertEquals(Screen_bar(null),routed_bar)
        val routed_nomatch= routing(router,"/unrec")
        assertTrue { routed_nomatch is Screen_nomatch  }
    }
    @Test
    fun simple_noMatch() {
        val router = MockRouter()
        val routing = RoutingResolver {
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
        val routed_foo= routing(router,"/foo")
        assertEquals(Screen_foo(null),routed_foo)
        val routed_bar= routing(router,"/bar")
        assertEquals(Screen_bar(null),routed_bar)
        val routed_nomatch= routing(router,"/unrec")
        assertTrue { routed_nomatch is Screen_nomatch  }
    }
    @Test
    fun simple_with_param() {
        val router = MockRouter()
        val routing = RoutingResolver {
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

        val routed_foo= routing(router,"/foo")
        assertEquals(Screen_foo(null),routed_foo)

        val routed_foo_with_arg= routing(router,"/foo/1")
        assertEquals(Screen_foo(1),routed_foo_with_arg)

        val routed_nomatch = routing(router,"/unrecognized_root")
        assertTrue { routed_nomatch is Screen_nomatch  }
    }
    @Test
    fun explicitely_access_params_and_remaniningpath() {
        val router = MockRouter()
        val routing = RoutingResolver {
            noMatch {
                var str="other$remainingPath"
                val parameters = parameters
                if (parameters != null) {
                    str += parameters.raw
                }
                Screen_with_string(str)
            }
        }
        var routed= routing(router,"/")
        assertEquals(Screen_with_string("other/"), routed)

        routed = routing(router,"/foo")
        assertEquals(Screen_with_string("other/foo"),routed)

        routed = routing(router,"/foo?${Parameters.from("V" to "b")}")
        assertEquals(Screen_with_string("other/fooV=b"),routed)
    }
}

