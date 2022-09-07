package com.beyondeye.kbloc.router

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

interface RouterWithRawPathListener:Router {
    fun setupRawPathListener(initPath: String)
    fun removeRawPathListener()
}