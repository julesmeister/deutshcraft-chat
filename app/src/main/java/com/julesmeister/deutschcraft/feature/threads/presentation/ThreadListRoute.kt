package com.julesmeister.deutschcraft.feature.threads.presentation

import androidx.compose.runtime.Composable

@Composable
fun ThreadListRoute(
    onThreadClick: (String) -> Unit
) {
    ThreadListScreen(
        onThreadClick = onThreadClick
    )
}