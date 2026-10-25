package com.julesmeister.deutschcraft.feature.archivedthreads.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.archivedthreads.domain.ArchivedThread
import com.julesmeister.deutschcraft.feature.archivedthreads.presentation.component.ArchivedThreadActionDialog
import com.julesmeister.deutschcraft.feature.archivedthreads.presentation.component.ArchivedThreadDeleteDialog
import com.julesmeister.deutschcraft.feature.archivedthreads.presentation.component.ArchivedThreadRow
import com.julesmeister.deutschcraft.feature.archivedthreads.presentation.component.ArchivedThreadsEmptyState
import com.julesmeister.deutschcraft.feature.archivedthreads.presentation.component.ArchivedThreadsTopBar

@Composable
fun ArchivedThreadsScreen(
    uiState: ArchivedThreadsUiState,
    onBackClick: () -> Unit,
    onThreadClick: (String) -> Unit,
    onRestoreClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    var selectedThreadForActions by remember {
        mutableStateOf<ArchivedThread?>(null)
    }

    var threadPendingDeletion by remember {
        mutableStateOf<ArchivedThread?>(null)
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
            ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(
            message = message
        )

        onDismissError()
    }

    LaunchedEffect(uiState.threads) {
        val existingThreadIds = uiState.threads
            .map { thread ->
                thread.id
            }
            .toSet()

        val selectedActionThreadId =
            selectedThreadForActions?.id

        if (
            selectedActionThreadId != null &&
            selectedActionThreadId !in existingThreadIds
        ) {
            selectedThreadForActions = null
        }

        val pendingDeletionThreadId =
            threadPendingDeletion?.id

        if (
            pendingDeletionThreadId != null &&
            pendingDeletionThreadId !in existingThreadIds
        ) {
            threadPendingDeletion = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ArchivedThreadsTopBar(
                onBackClick = onBackClick
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.threads.isEmpty() -> {
                ArchivedThreadsEmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom =
                            innerPadding.calculateBottomPadding() +
                                    24.dp
                    )
                ) {
                    items(
                        items = uiState.threads,
                        key = { thread ->
                            thread.id
                        }
                    ) { thread ->
                        ArchivedThreadRow(
                            thread = thread,
                            isProcessing =
                                uiState.processingThreadId ==
                                        thread.id,
                            onClick = {
                                onThreadClick(thread.id)
                            },
                            onSwipeActions = {
                                selectedThreadForActions = thread
                            }
                        )
                    }
                }
            }
        }
    }

    selectedThreadForActions?.let { thread ->
        ArchivedThreadActionDialog(
            thread = thread,
            onDismiss = {
                selectedThreadForActions = null
            },
            onRestoreClick = {
                selectedThreadForActions = null

                onRestoreClick(
                    thread.id
                )
            },
            onDeleteClick = {
                selectedThreadForActions = null
                threadPendingDeletion = thread
            }
        )
    }

    threadPendingDeletion?.let { thread ->
        ArchivedThreadDeleteDialog(
            thread = thread,
            onDismiss = {
                threadPendingDeletion = null
            },
            onConfirmDelete = {
                threadPendingDeletion = null

                onDeleteClick(
                    thread.id
                )
            }
        )
    }
}