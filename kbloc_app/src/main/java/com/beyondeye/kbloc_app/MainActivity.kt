package com.beyondeye.kbloc_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.beyondeye.kbloc.compose.navigator.LocalNavigator
import com.beyondeye.kbloc.compose.navigator.Navigator
import com.beyondeye.kbloc.compose.navigator.currentOrThrow
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc_app.ui.theme.Compose_blocTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Navigator(MainScreen())
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
                        Button(onClick={ navigator.push(Test1Screen())}) { Text("click for test1") }
                        Button(onClick={ navigator.push(Test2Screen())}) { Text("click for test2") }
                    }
                }

            )
        }
    }
}

class Test1Screen: Screen {
    @Composable
    override fun Content() {
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