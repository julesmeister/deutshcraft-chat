package com.julesmeister.deutschcraft.feature.threads.presentation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ThreadListRoute(
    onThreadClick: (String) -> Unit,
    onArchivedThreadsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ThreadListViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    /*
     * The Route owns the SnackbarHostState.
     *
     * The same instance is passed to ThreadListScreen,
     * where ThreadListScaffold renders the Snackbar host.
     */
    val snackbarHostState =
        remember {
            SnackbarHostState()
        }

    /*
     * Collect one-time navigation events.
     *
     * This handles:
     *
     * - Newly created regular conversations
     * - Newly started recommended mission conversations
     */
    LaunchedEffect(viewModel) {
        viewModel.navigationEvents.collect { event ->
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

    /*
     * Show the archive confirmation only after the database
     * operation succeeds.
     */
    LaunchedEffect(viewModel) {
        viewModel.archiveSnackbarEvents.collect { event ->
            val snackbarResult =
                snackbarHostState.showSnackbar(
                    message =
                        buildString {
                            append("\"")
                            append(
                                event.conversationTitle
                            )
                            append("\" archiviert")
                        },
                    actionLabel =
                        "RÜCKGÄNGIG",
                    withDismissAction =
                        true,
                    duration =
                        SnackbarDuration.Long
                )

            if (
                snackbarResult ==
                SnackbarResult.ActionPerformed
            ) {
                viewModel.restoreThread(
                    conversationId =
                        event.conversationId
                )
            }
        }
    }

    /*
     * Display database, repository, or mission-start errors
     * with the same Snackbar host.
     */
    LaunchedEffect(
        uiState.errorMessage
    ) {
        val errorMessage =
            uiState.errorMessage
                ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(
            message =
                errorMessage,
            withDismissAction =
                true,
            duration =
                SnackbarDuration.Long
        )

        viewModel.dismissError()
    }

    ThreadListScreen(
        uiState =
            uiState,
        snackbarHostState =
            snackbarHostState,
        onThreadClick =
            onThreadClick,
        onRecommendedMissionClick =
            viewModel::startRecommendedMission,
        onArchivedThreadsClick =
            onArchivedThreadsClick,
        onNewThreadClick =
            viewModel::createThread,
        onArchiveThread =
            viewModel::archiveThread,
        onDeleteThread =
            viewModel::deleteThread,
        onErrorDismissed =
            viewModel::dismissError,
        modifier =
            modifier
    )
}