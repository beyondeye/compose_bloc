package com.beyondeye.kbloc.router

import com.beyondeye.kbloc.router.utils.MockRouter
import com.beyondeye.kbloc.router.utils.Screen_with_string
import kotlin.test.Test
import kotlin.test.assertEquals

public class StringRoutingTests {
    @Test
    public fun contentTest() {
        val router = MockRouter()
        val routing = RoutingResolver("/") {
            route("foo") {
                noMatch {
                    Screen_with_string("foo")
                }
            }
            string {
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

        router.navigate("/bar")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("barbar"), routed)
    }
    @Test
    public fun routeTest() {
        val router = MockRouter()
        val routing = RoutingResolver("/") {
            route("users") {
                string { userID ->
                    route("todos") {
                        string {
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

        router.navigate("/users/john")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("UserInfo: john"), routed)

        router.navigate("/users/john/todos")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("All todos for user: john"), routed)

        router.navigate("/users/john/todos/first")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("Todo first for user: john"), routed)
    }
    @Test
    public fun nested() {
        val router = MockRouter()
        val routing = RoutingResolver("/") {
            string { userID ->
                string { todoID ->
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

        router.navigate("/f")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("User f"), routed)

        router.navigate("/f/t")
        routed = routing.resolveFor(router)
        assertEquals(Screen_with_string("Todo with t from user f"), routed)
    }
}