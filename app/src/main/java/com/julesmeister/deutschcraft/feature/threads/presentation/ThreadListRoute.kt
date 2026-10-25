package com.julesmeister.deutschcraft.feature.threads.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
@Composable
fun ThreadListRoute(
    onThreadClick: (String) -> Unit,
    onArchivedThreadsClick: () -> Unit,
    viewModel: ThreadListViewModel =
        hiltViewModel()
) {
    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.navigationEvents
            .collect { event ->
                when (event) {
                    is ThreadListNavigationEvent
                    .OpenConversation -> {
                        onThreadClick(
                            event.conversationId
                        )
                    }
                }
            }
    }

    ThreadListScreen(
        uiState = uiState,
        onThreadClick = onThreadClick,
        onArchivedThreadsClick =
            onArchivedThreadsClick,
        onNewThreadClick =
            viewModel::createThread,
        onArchiveThread =
            viewModel::archiveThread,
        onDeleteThread =
            viewModel::deleteThread,
        onErrorDismissed =
            viewModel::dismissError
    )
}