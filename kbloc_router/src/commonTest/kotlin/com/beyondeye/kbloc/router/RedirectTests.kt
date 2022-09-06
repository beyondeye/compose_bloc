package com.beyondeye.kbloc.router

import com.beyondeye.kbloc.router.utils.MockRouter
import kotlin.test.Test
import kotlin.test.assertEquals

public class RedirectTests {
    @Test
    public fun redirectingTest() {
        val router = MockRouter()
        val routing = RoutingResolver("/") {
            route("foo") {
                noMatch {
                    Screen_foo(null)
                }
            }
            redirect("bar", "baz", target = "foo")
            noMatch {
                Screen_nomatch()
            }
        }

        router.navigate("/foo")
        var routed=routing.resolveFor(router)
        assertEquals(Screen_foo(null),routed)

        router.navigate("/bar")
        routed=routing.resolveFor(router)
        assertEquals(Screen_foo(null),routed)

        router.navigate("/baz")
        routed=routing.resolveFor(router)
        assertEquals(Screen_foo(null),routed)
    }
    @Test
    public fun redirectingNoMatchTest() {
        val router = MockRouter()
        val routing = RoutingResolver("/bar") {
            route("foo") {
                Screen_foo(null)
            }
            route("bar") {
                Screen_bar(null)
            }
            noMatch {
                redirect("foo", hide = true)
            }
        }

        //check that default path is resolved correctly
        var routed=routing.resolveFor(router)
        assertEquals(Screen_bar(null), routed)

        router.navigate("/asdf")
        routed=routing.resolveFor(router)
        assertEquals(Screen_foo(null), routed)
    }

}