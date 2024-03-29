package com.beyondeye.kbloc_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.RootNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
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
                        Button(onClick={ navigator.push(Test1BasicCounterBlocScreen())}) { Text("Basic Counter Bloc") }
                        Button(onClick={ navigator.push(Test2BasicBlocListenerScreen())}) { Text("Basic Counter Bloc Listener") }
                        Button(onClick={ navigator.push(Test3BasicBlocConsumerScreen())}) { Text("Basic Counter Bloc Consumer") }
                        Button(onClick={ navigator.push(Test4MultiBlocProviderScreen())}) { Text("Basic Counter MultiBloc provider") }
                        Button(onClick={ navigator.push(Test5BlocSelector())}) { Text("MultiCounter BlocSelector") }
                        Button(onClick={ navigator.push(Test6BlocSelectorWithAbstractSelector())}) { Text("MultiCounter BlocSelector with AbstractSelector") }
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