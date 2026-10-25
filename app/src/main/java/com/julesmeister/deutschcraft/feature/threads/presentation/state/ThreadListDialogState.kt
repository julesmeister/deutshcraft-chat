package com.julesmeister.deutschcraft.feature.threads.presentation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberThreadListDialogsState():
        ThreadListDialogsState {
    return remember {
        ThreadListDialogsState()
    }
}