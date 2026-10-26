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

    /*
     * ThreadListRoute normally handles errors using the
     * shared SnackbarHostState.
     *
     * This remains here so ThreadListScreen can still work
     * correctly if it is used without the Route.
     */
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

    /*
     * Hide the archive row if there are no longer any
     * archived conversations.
     */
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
        onNewThreadClick =
            onNewThreadClick
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