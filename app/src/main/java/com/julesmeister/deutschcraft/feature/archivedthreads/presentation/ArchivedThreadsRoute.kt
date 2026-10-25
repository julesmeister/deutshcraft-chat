package com.julesmeister.deutschcraft.feature.archivedthreads.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.deutschcraft.feature.archivedthreads.presentation.ArchivedThreadsScreen

@Composable
fun ArchivedThreadsRoute(
    onBackClick: () -> Unit,
    onThreadClick: (String) -> Unit,
    viewModel: ArchivedThreadsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    ArchivedThreadsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onThreadClick = onThreadClick,
        onRestoreClick = viewModel::restoreThread,
        onDeleteClick = viewModel::deleteThreadPermanently,
        onDismissError = viewModel::dismissError
    )
}