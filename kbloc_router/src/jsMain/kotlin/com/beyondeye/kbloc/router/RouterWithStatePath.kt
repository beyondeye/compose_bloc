package com.beyondeye.kbloc.router

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

interface RouterWithStatePath:Router {
    @Composable
    fun getCurrentRawPathAsState(initPath: String): State<String>
}