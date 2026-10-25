package com.julesmeister.deutschcraft.feature.threads.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import com.julesmeister.deutschcraft.ui.theme.DeutschError
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschOutlineSoft
import com.julesmeister.deutschcraft.ui.theme.DeutschSurface
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHigh
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadListScreen(
    uiState: ThreadListUiState,
    onThreadClick: (String) -> Unit,
    onNewThreadClick: () -> Unit,
    onArchiveThread: (String) -> Unit,
    onDeleteThread: (String) -> Unit,
    onErrorDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    var threadPendingDeletion by remember {
        mutableStateOf<ConversationThread?>(null)
    }

    LaunchedEffect(uiState.errorMessage) {
        val errorMessage =
            uiState.errorMessage
                ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(
            message = errorMessage
        )

        onErrorDismissed()
    }

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
            FloatingActionButton(
                onClick = onNewThreadClick,
                containerColor = DeutschLime,
                contentColor = DeutschTextOnLime,
                shape = CircleShape
            ) {
                if (uiState.isCreatingThread) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = DeutschTextOnLime,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription =
                            "Neue Unterhaltung"
                    )
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                ThreadListLoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            uiState.isEmpty -> {
                ThreadListEmptyContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onNewThreadClick =
                        onNewThreadClick
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 12.dp,
                        end = 16.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    uiState.sections.forEach { section ->
                        item(
                            key = "header-${section.id}"
                        ) {
                            ThreadSectionHeader(
                                title = section.title
                            )
                        }

                        items(
                            items = section.threads,
                            key = { thread ->
                                thread.id
                            }
                        ) { thread ->
                            ThreadCard(
                                thread = thread,
                                onClick = {
                                    onThreadClick(
                                        thread.id
                                    )
                                },
                                onArchiveClick = {
                                    onArchiveThread(
                                        thread.id
                                    )
                                },
                                onDeleteClick = {
                                    threadPendingDeletion =
                                        thread
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    val pendingDeletion =
        threadPendingDeletion

    if (pendingDeletion != null) {
        DeleteThreadDialog(
            threadTitle =
                pendingDeletion.displayTitle,
            onConfirm = {
                onDeleteThread(
                    pendingDeletion.id
                )

                threadPendingDeletion = null
            },
            onDismiss = {
                threadPendingDeletion = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThreadListTopBar() {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Unterhaltungen",
                    color = DeutschTextPrimary,
                    style =
                        MaterialTheme.typography
                            .titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text =
                        "Deine täglichen Deutsch-Gespräche",
                    color = DeutschTextSecondary,
                    style =
                        MaterialTheme.typography
                            .bodySmall
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor =
                    MaterialTheme.colorScheme
                        .background,
                titleContentColor =
                    DeutschTextPrimary
            )
    )
}

@Composable
private fun ThreadSectionHeader(
    title: String
) {
    Text(
        modifier = Modifier.padding(
            top = 10.dp,
            bottom = 2.dp
        ),
        text = title,
        color = DeutschTextSecondary,
        style =
            MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun ThreadCard(
    thread: ConversationThread,
    onClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(DeutschSurfaceHigh)
            .clickable(onClick = onClick)
            .padding(
                start = 16.dp,
                top = 16.dp,
                bottom = 16.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {
        LevelBadge(
            level = thread.cefrLevel.name,
            isClosed = thread.isClosed
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = thread.displayTitle,
                    color = DeutschTextPrimary,
                    style =
                        MaterialTheme.typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                if (thread.isClosed) {
                    Icon(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(14.dp),
                        imageVector =
                            Icons.Outlined.Lock,
                        contentDescription =
                            "Geschlossene Unterhaltung",
                        tint = DeutschTextSecondary
                    )
                }
            }

            Text(
                modifier = Modifier.padding(
                    top = 5.dp
                ),
                text = thread.displayPreview,
                color = DeutschTextSecondary,
                style =
                    MaterialTheme.typography
                        .bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.padding(
                    top = 10.dp
                ),
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = messageCountText(
                        count =
                            thread.messageCount
                    ),
                    color =
                        DeutschTextSecondary.copy(
                            alpha = 0.75f
                        ),
                    style =
                        MaterialTheme.typography
                            .labelMedium
                )

                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(
                            DeutschTextSecondary
                                .copy(alpha = 0.5f)
                        )
                )

                Text(
                    text = formatTime(
                        timestamp =
                            thread.updatedAt
                    ),
                    color =
                        DeutschTextSecondary.copy(
                            alpha = 0.75f
                        ),
                    style =
                        MaterialTheme.typography
                            .labelMedium
                )
            }
        }

        Box {
            IconButton(
                onClick = {
                    menuExpanded = true
                }
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.MoreVert,
                    contentDescription =
                        "Unterhaltungsoptionen",
                    tint = DeutschTextSecondary
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = {
                    menuExpanded = false
                },
                containerColor =
                    MaterialTheme.colorScheme
                        .surfaceContainerHigh
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Archivieren"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector =
                                Icons.Outlined.Archive,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onArchiveClick()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Löschen",
                            color = DeutschError
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector =
                                Icons.Outlined
                                    .DeleteOutline,
                            contentDescription = null,
                            tint = DeutschError
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onDeleteClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun LevelBadge(
    level: String,
    isClosed: Boolean
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
                if (isClosed) {
                    DeutschSurface
                } else {
                    DeutschLime.copy(
                        alpha = 0.15f
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = level,
            color =
                if (isClosed) {
                    DeutschTextSecondary
                } else {
                    DeutschLime
                },
            style =
                MaterialTheme.typography
                    .labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ThreadListLoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = DeutschLime,
            strokeWidth = 3.dp
        )
    }
}

@Composable
private fun ThreadListEmptyContent(
    onNewThreadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    DeutschLime.copy(
                        alpha = 0.14f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector =
                    Icons.Outlined.Forum,
                contentDescription = null,
                tint = DeutschLime
            )
        }

        Text(
            modifier = Modifier.padding(
                top = 20.dp
            ),
            text = "Noch keine Unterhaltungen",
            color = DeutschTextPrimary,
            style =
                MaterialTheme.typography
                    .titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            modifier = Modifier.padding(
                top = 8.dp
            ),
            text =
                "Beginne eine Unterhaltung auf " +
                        "Deutsch. Deine Gespräche werden " +
                        "automatisch nach Datum organisiert.",
            color = DeutschTextSecondary,
            style =
                MaterialTheme.typography
                    .bodyMedium
        )

        TextButton(
            modifier = Modifier.padding(
                top = 14.dp
            ),
            onClick = onNewThreadClick
        ) {
            Text(
                text = "Unterhaltung beginnen",
                color = DeutschLime,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DeleteThreadDialog(
    threadTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeutschSurfaceHigh,
        titleContentColor = DeutschTextPrimary,
        textContentColor = DeutschTextSecondary,
        shape = RoundedCornerShape(26.dp),
        title = {
            Text(
                text = "Unterhaltung löschen?"
            )
        },
        text = {
            Text(
                text =
                    "\"$threadTitle\" und alle " +
                            "zugehörigen Nachrichten werden " +
                            "dauerhaft gelöscht."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = "Löschen",
                    color = DeutschError,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Abbrechen",
                    color = DeutschTextSecondary
                )
            }
        }
    )
}

private fun messageCountText(
    count: Int
): String {
    return if (count == 1) {
        "1 Nachricht"
    } else {
        "$count Nachrichten"
    }
}

private fun formatTime(
    timestamp: Long
): String {
    return SimpleDateFormat(
        "HH:mm",
        Locale.GERMAN
    ).format(
        Date(timestamp)
    )
}