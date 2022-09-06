package com.beyondeye.kbloc.router.utils

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

internal data class Screen_foo(val arg: Int?) : Screen {
    @Composable
    override fun Content() {
    }
}

internal data class Screen_bar(val arg: Int?) : Screen {
    @Composable
    override fun Content() {
    }
}

internal class Screen_nomatch : Screen {
    @Composable
    override fun Content() {
    }
}

internal data class Screen_with_string(val str: String) : Screen {
    @Composable
    override fun Content() {
    }
}