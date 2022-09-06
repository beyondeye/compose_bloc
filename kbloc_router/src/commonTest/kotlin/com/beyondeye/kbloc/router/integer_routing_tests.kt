package com.beyondeye.kbloc.router

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class IntegerRoutingTests {
    @Test
    public fun contentTest() {
        val router = MockRouter()
        val routing = RoutingResolver("/") {
            route("foo") {
                noMatch {
                    Screen_with_string("foo")
                }
            }
            int {
                Screen_with_string("bar$it")
            }
            noMatch {
                Screen_with_string("other")
            }
        }
        var routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("other"), routed)

        router.navigate("/foo")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("foo"), routed)

        router.navigate("/5")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("bar5"), routed)

    }
}