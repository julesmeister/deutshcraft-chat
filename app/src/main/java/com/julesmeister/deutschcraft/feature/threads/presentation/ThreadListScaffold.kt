package com.julesmeister.deutschcraft.feature.threads.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.threads.presentation.component.CreateThreadFab
import com.julesmeister.deutschcraft.feature.threads.presentation.component.DeutschCraftSnackbar
import com.julesmeister.deutschcraft.feature.threads.presentation.component.ThreadListTopBar

@Composable
fun ThreadListScaffold(
    snackbarHostState: SnackbarHostState,
    isCreatingThread: Boolean,
    onNewThreadClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ThreadListTopBar()
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
            ) { snackbarData ->
                DeutschCraftSnackbar(
                    snackbarData = snackbarData
                )
            }
        },
        floatingActionButton = {
            CreateThreadFab(
                isCreatingThread = isCreatingThread,
                onClick = onNewThreadClick
            )
        },
        content = content
    )
}