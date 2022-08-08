package com.beyondeye.kbloc_app

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc_app.ui.theme.Compose_blocTheme

class Test2Screen: Screen {
    @Composable
    override fun Content() {
        Compose_blocTheme {
            Scaffold(
                topBar = { TopAppBar { Text("KBloc test app") } },
                backgroundColor = MaterialTheme.colors.background,
                content = { paddingvalues ->
                    Text("Test2 screen")
                }
            )
        }
    }
}