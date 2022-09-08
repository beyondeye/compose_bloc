import cafe.adriel.voyager.core.screen.Screen
import com.beyondeye.kbloc.router.RouteResolver
import screens.*

object AppRoutes {
    val home = "home"
    val testbasic = "testbasic"
    fun testbasic_(testNumber:Int)="$testbasic/$testNumber"
    val counter = "counter"
    val seg_counter_bloc = "bloc"
    val seg_counter_cubit="cubit"
    val counter_bloc = "$counter/$seg_counter_bloc"
    val counter_cubit = "$counter/$seg_counter_cubit"
    //-----------------
    val resolver = RouteResolver("/") {
        route(AppRoutes.home) {
            MainScreen()
        }
        route(AppRoutes.testbasic) {
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
        route(AppRoutes.counter) {
            route(AppRoutes.seg_counter_bloc) {
                TestBasicCounterBlocScreenWeb()
            }
            route(AppRoutes.seg_counter_cubit) {
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
}

