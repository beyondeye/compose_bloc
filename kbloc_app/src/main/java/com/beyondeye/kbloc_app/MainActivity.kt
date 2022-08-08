package com.beyondeye.kbloc_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import com.beyondeye.kbloc.compose.android.screen.RootNavigator
import com.beyondeye.kbloc.compose.navigator.LocalNavigator
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