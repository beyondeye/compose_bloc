package com.beyondeye.kbloc_app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beyondeye.kbloc.compose.android.screen.AndroidScreen
import com.beyondeye.kbloc.compose.android.screen.RootNavigator
import com.beyondeye.kbloc.compose.navigator.LocalNavigator
import com.beyondeye.kbloc.compose.navigator.Navigator
import com.beyondeye.kbloc.compose.navigator.currentOrThrow
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc_app.ui.theme.Compose_blocTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //*IMPORTANT* need to use RootNavigator to initialize navigator for activity, not as in original voyager
            RootNavigator(MainScreen())
        }
    }
}

class MainScreen: Screen {
    @Composable
    override fun Content() {
        val navigator= LocalNavigator.currentOrThrow
        Compose_blocTheme {
            Scaffold(
                topBar = { TopAppBar{ Text("KBloc test app") } },
                backgroundColor = MaterialTheme.colors.background,
                content = { paddingvalues ->
                    Column {
                        Button(onClick={ navigator.push(Test0Screen())}) { Text("click for test0") }
                        Button(onClick={ navigator.push(Test1Screen())}) { Text("click for test1") }
                        Button(onClick={ navigator.push(Test2Screen())}) { Text("click for test2") }
                    }
                }

            )
        }
    }
}


class RegularViewModel  : ViewModel() {

    val items = listOf<String>()
}
//when screen is rotated, screen is NOT recreated but screenmodel is recreated
class Test0Screen : AndroidScreen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        // Uncomment version below if you want keep using ViewModel instead of to convert it to ScreenModel
        // val viewModel: HiltListViewModel = getViewModel()
//        val viewModel: HiltListScreenModel = getScreenModel()
        val viewModel:RegularViewModel = viewModel()
        Log.e("!!!!!", "!!!!!$viewModel")
        Text(text = "This is screen: $this with screen model: $viewModel")
    }
}

class Test1Screen: Screen {
    @Composable
    override fun Content() {
        Log.d("*plensee*","created")
        DisposableEffect(true) {
            onDispose { Log.d("*plensee*","disposed") }
        }
        Compose_blocTheme {
            Scaffold(
                topBar = { TopAppBar{ Text("KBloc test app") } },
                backgroundColor = MaterialTheme.colors.background,
                content = { paddingvalues ->
                    Text("Test1 screen")
                }
            )
        }
    }
}
class Test2Screen: Screen {
    @Composable
    override fun Content() {
        Compose_blocTheme {
            Scaffold(
                topBar = { TopAppBar{ Text("KBloc test app") } },
                backgroundColor = MaterialTheme.colors.background,
                content = { paddingvalues ->
                    Text("Test2 screen")
                }
            )
        }
    }
}



@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Compose_blocTheme {
        Greeting("Android")
    }
}