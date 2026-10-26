package com.julesmeister.deutschcraft.feature.threads.presentation

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.julesmeister.deutschcraft.feature.threads.presentation.component.ThreadDialogs
import com.julesmeister.deutschcraft.feature.threads.presentation.component.rememberThreadArchivePullState
import com.julesmeister.deutschcraft.feature.threads.presentation.state.rememberThreadListDialogsState

@Composable
fun ThreadListScreen(
    uiState: ThreadListUiState,
    snackbarHostState: SnackbarHostState,
    onThreadClick: (String) -> Unit,
    onRecommendedMissionClick: () -> Unit,
    onArchivedThreadsClick: () -> Unit,
    onNewThreadClick: () -> Unit,
    onProgressClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onArchiveThread: (String) -> Unit,
    onDeleteThread: (String) -> Unit,
    onErrorDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState =
        rememberLazyListState()

    val archivePullState =
        rememberThreadArchivePullState(
            listState =
                listState,
            archiveEnabled =
                uiState.hasArchivedThreads
        )

    val dialogsState =
        rememberThreadListDialogsState()

    LaunchedEffect(
        uiState.errorMessage
    ) {
        val errorMessage =
            uiState.errorMessage
                ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(
            message =
                errorMessage
        )

        onErrorDismissed()
    }

    LaunchedEffect(
        uiState.hasArchivedThreads
    ) {
        if (!uiState.hasArchivedThreads) {
            archivePullState.hideArchiveRow()
        }
    }

    ThreadListScaffold(
        modifier =
            modifier,
        snackbarHostState =
            snackbarHostState,
        isCreatingThread =
            uiState.isCreatingThread ||
                    uiState.isStartingRecommendedMission,
        streakDays =
            uiState.currentStreak,
        onProgressClick =
            onProgressClick,
        onNewThreadClick =
            onNewThreadClick,
        onSettingsClick =
            onSettingsClick
    ) { innerPadding ->
        ThreadListContent(
            uiState =
                uiState,
            archivePullState =
                archivePullState,
            listState =
                listState,
            innerPadding =
                innerPadding,
            onThreadClick =
                onThreadClick,
            onRecommendedMissionClick =
                onRecommendedMissionClick,
            onNewThreadClick =
                onNewThreadClick,
            onArchivedThreadsClick =
                onArchivedThreadsClick,
            onThreadActionsRequested = { thread ->
                dialogsState.showActions(
                    thread =
                        thread
                )
            }
        )
    }

    ThreadDialogs(
        dialogsState =
            dialogsState,
        onArchiveThread =
            onArchiveThread,
        onDeleteThread =
            onDeleteThread
    )
}