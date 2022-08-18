package com.beyondeye.kbloc_app

import android.util.Log
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.beyondeye.kbloc.core.LOGTAG

class RegularViewModel  : ViewModel() {

    val items = listOf<String>()
}

//when screen is rotated, screen is NOT recreated but screenmodel is recreated
class Test0Screen : AndroidScreen(),Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        // Uncomment version below if you want keep using ViewModel instead of to convert it to ScreenModel
        // val viewModel: HiltListViewModel = getViewModel()
//        val viewModel: HiltListScreenModel = getScreenModel()
        val viewModel: RegularViewModel = viewModel()
        Log.e(LOGTAG, "!!!!!$viewModel")
        Text(text = "This is screen: $this with screen model: $viewModel")
    }
}