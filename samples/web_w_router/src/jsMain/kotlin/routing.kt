import com.beyondeye.kbloc.router.RouteResolver
import screens.*

val app_routing = RouteResolver("/") {
    route("main") {
        MainScreen()
    }
    route("testbasic") {
        int {
            when(it) {
                1 -> TestBasicScreen1()
                2 -> TestBasicScreen2()
                else -> MainScreen()
            }
        }
        noMatch {
            MainScreen()
        }
    }
    route("counter") {
        route("bloc") {
            TestBasicCounterBlocScreenWeb()
        }
        route("cubit") {
            TestBasicCounterCubitScreenWeb()
        }
        noMatch {
            MainScreen()
        }
    }
    noMatch {
        MainScreen()
    }
}