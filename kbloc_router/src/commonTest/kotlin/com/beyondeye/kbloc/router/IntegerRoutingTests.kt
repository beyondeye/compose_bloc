package com.beyondeye.kbloc.router

import com.beyondeye.kbloc.router.utils.MockRouter
import com.beyondeye.kbloc.router.utils.Screen_with_string
import kotlin.test.Test
import kotlin.test.assertEquals

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
    @Test
    public fun routeTest() {
        val router = MockRouter()
        val routing = RoutingResolver("/") {
            route("users") {
                int { userID ->
                    route("todos") {
                        int {
                            Screen_with_string("Todo $it for user: $userID")
                        }
                        noMatch {
                            Screen_with_string("All todos for user: $userID")
                        }
                    }
                    noMatch {
                        Screen_with_string("UserInfo: $userID")
                    }
                }
                noMatch {
                    Screen_with_string("No userID")
                }
            }
            noMatch {
                Screen_with_string("other")
            }
        }
        var routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("other"), routed)

        router.navigate("/users")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("No userID"), routed)

        router.navigate("/users/5")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("UserInfo: 5"), routed)

        router.navigate("/users/5/todos")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("All todos for user: 5"), routed)

        router.navigate("/users/5/todos/42")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("Todo 42 for user: 5"), routed)
    }
    @Test
    public fun nested() {
        val router = MockRouter()
        val routing = RoutingResolver("/") {
            int { userID ->
                int { todoID ->
                    Screen_with_string("Todo with $todoID from user $userID")
                }
                noMatch {
                    Screen_with_string("User $userID")
                }
            }
            noMatch {
                Screen_with_string("No userID given")
            }

        }
        var routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("No userID given"), routed)

        router.navigate("/42")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("User 42"), routed)

        router.navigate("/42/42")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("Todo with 42 from user 42"), routed)
    }

    @Test
    public fun invalid() {
        val router = MockRouter()
        val routing = RoutingResolver("/") {
            int {
                Screen_with_string("int $it")
            }
            noMatch {
                Screen_with_string("noMatch")
            }
        }
        var routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("noMatch"), routed)

        router.navigate("/foo")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("noMatch"), routed)
    }

}