package com.julesmeister.deutschcraft.feature.threads.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.julesmeister.deutschcraft.feature.threads.presentation.component.CreateThreadFab
import com.julesmeister.deutschcraft.feature.threads.presentation.component.ThreadListTopBar

@Composable
fun ThreadListScaffold(
    snackbarHostState: SnackbarHostState,
    isCreatingThread: Boolean,
    onNewThreadClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (
        innerPadding: PaddingValues
    ) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor =
            MaterialTheme.colorScheme.background,
        topBar = {
            ThreadListTopBar()
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        floatingActionButton = {
            CreateThreadFab(
                isCreatingThread =
                    isCreatingThread,
                onClick =
                    onNewThreadClick
            )
        },
        content = content
    )
}