package com.beyondeye.kbloc_app

import android.util.Log
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.beyondeye.kbloc.compose.screen.Screen
import com.beyondeye.kbloc.core.LOGTAG
import com.beyondeye.kbloc_app.ui.theme.Compose_blocTheme

class Test1Screen: Screen {
    @Composable
    override fun Content() {
        Log.d(LOGTAG, "created")
        DisposableEffect(true) {
            onDispose { Log.d(LOGTAG, "disposed") }
        }
        Compose_blocTheme {
            Scaffold(
                topBar = { TopAppBar { Text("KBloc test app") } },
                backgroundColor = MaterialTheme.colors.background,
                content = { paddingvalues ->
                    Text("Test1 screen")
                }
            )
        }
    }
}