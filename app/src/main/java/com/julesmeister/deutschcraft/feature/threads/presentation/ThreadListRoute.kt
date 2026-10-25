package com.julesmeister.deutschcraft.feature.threads.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ThreadListRoute(
    onThreadClick: (String) -> Unit,
    onNewThreadClick: () -> Unit,
    viewModel: ThreadListViewModel =
        hiltViewModel()
) {
    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    ThreadListScreen(
        uiState = uiState,
        onThreadClick = onThreadClick,
        onNewThreadClick =
            onNewThreadClick,
        onArchiveThread = {
                conversationId ->
            viewModel.archiveThread(
                conversationId =
                    conversationId
            )
        },
        onDeleteThread = {
                conversationId ->
            viewModel.deleteThread(
                conversationId =
                    conversationId
            )
        },
        onErrorDismissed =
            viewModel::dismissError
    )
}