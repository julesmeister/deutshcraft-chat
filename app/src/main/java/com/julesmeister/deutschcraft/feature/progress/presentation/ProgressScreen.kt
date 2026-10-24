package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ChatInputBar
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ChatOverflowMenu
import com.julesmeister.deutschcraft.feature.chat.presentation.component.MessageBubble
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ReplySuggestions
import com.julesmeister.deutschcraft.feature.chat.presentation.component.TypingIndicator
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHigh
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onAction: (ChatUiAction) -> Unit,
    onProgressClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNewConversationClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onClearConversationClick: () -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    val listState = rememberLazyListState()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val displayedItemCount =
        uiState.messages.size +
                if (uiState.isAssistantTyping) {
                    1
                } else {
                    0
                }

    /*
     * Scroll to the newest item whenever a message
     * or typing indicator is added.
     */
    LaunchedEffect(displayedItemCount) {
        if (displayedItemCount > 0) {
            listState.animateScrollToItem(
                index = displayedItemCount - 1
            )
        }
    }

    /*
     * Display errors once through a Snackbar,
     * then remove the error from the UI state.
     */
    LaunchedEffect(uiState.errorMessage) {
        val errorMessage =
            uiState.errorMessage
                ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(
            message = errorMessage
        )

        onAction(ChatUiAction.ErrorDismissed)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor =
            MaterialTheme.colorScheme.background,
        topBar = {
            DeutschCraftChatTopBar(
                isAssistantTyping =
                    uiState.isAssistantTyping,
                isAssessing =
                    uiState.isAssessing,
                onProgressClick =
                    onProgressClick,
                onNewConversationClick =
                    onNewConversationClick,
                onSettingsClick =
                    onSettingsClick,
                onClearConversationClick =
                    onClearConversationClick
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        bottomBar = {
            ChatBottomBar(
                uiState = uiState,
                onAction = onAction,
                onAddClick = onAddClick
            )
        }
    ) { innerPadding ->
        ChatMessageList(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background
                )
                .padding(innerPadding),
            uiState = uiState,
            listState = listState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeutschCraftChatTopBar(
    isAssistantTyping: Boolean,
    isAssessing: Boolean,
    onProgressClick: () -> Unit,
    onNewConversationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onClearConversationClick: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            DeutschCraftAvatar()
        },
        title = {
            Column(
                modifier = Modifier.padding(
                    start = 8.dp
                )
            ) {
                Text(
                    text = "DeutschCraft",
                    color = DeutschTextPrimary,
                    style =
                        MaterialTheme.typography
                            .titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = assistantStatusText(
                        isAssistantTyping =
                            isAssistantTyping,
                        isAssessing = isAssessing
                    ),
                    color = assistantStatusColor(
                        isAssistantTyping =
                            isAssistantTyping,
                        isAssessing = isAssessing
                    ),
                    style =
                        MaterialTheme.typography
                            .bodySmall
                )
            }
        },
        actions = {
            ProgressButton(
                onClick = onProgressClick
            )

            ChatOverflowMenu(
                onNewConversationClick =
                    onNewConversationClick,
                onSettingsClick =
                    onSettingsClick,
                onClearConversationClick =
                    onClearConversationClick
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor =
                MaterialTheme.colorScheme.background,
            titleContentColor =
                DeutschTextPrimary,
            actionIconContentColor =
                DeutschTextPrimary,
            navigationIconContentColor =
                DeutschTextPrimary
        )
    )
}

@Composable
private fun DeutschCraftAvatar() {
    Box(
        modifier = Modifier
            .padding(start = 12.dp)
            .size(44.dp)
            .clip(CircleShape)
            .background(DeutschLime),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "D",
            color = DeutschTextOnLime,
            style =
                MaterialTheme.typography
                    .titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProgressButton(
    onClick: () -> Unit
) {
    IconButton(
        modifier = Modifier
            .padding(end = 6.dp)
            .size(44.dp)
            .clip(CircleShape)
            .background(DeutschSurfaceHigh),
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Outlined.Insights,
            contentDescription =
                "View German progress",
            tint = DeutschLime
        )
    }
}

@Composable
private fun ChatBottomBar(
    uiState: ChatUiState,
    onAction: (ChatUiAction) -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.background
            )
            .navigationBarsPadding()
    ) {
        ReplySuggestions(
            suggestions = uiState.suggestions,
            onSuggestionClick = { suggestionId ->
                onAction(
                    ChatUiAction.SuggestionSelected(
                        suggestionId = suggestionId
                    )
                )
            }
        )

        ChatInputBar(
            value = uiState.messageInput,
            canSend = uiState.canSend,
            onValueChange = { value ->
                onAction(
                    ChatUiAction.MessageInputChanged(
                        value = value
                    )
                )
            },
            onSendClick = {
                onAction(ChatUiAction.SendMessage)
            },
            onAddClick = onAddClick
        )
    }
}

@Composable
private fun ChatMessageList(
    uiState: ChatUiState,
    listState:
    androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        if (
            uiState.messages.isEmpty() &&
            !uiState.isAssistantTyping
        ) {
            EmptyConversationContent(
                modifier = Modifier.align(
                    Alignment.Center
                )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement =
                Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(
                horizontal = 12.dp,
                vertical = 16.dp
            )
        ) {
            items(
                items = uiState.messages,
                key = { message ->
                    message.id
                }
            ) { message ->
                MessageBubble(
                    message = message
                )
            }

            if (uiState.isAssistantTyping) {
                item(
                    key = "typing-indicator"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        TypingIndicator()

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (
                uiState.isAssessing &&
                !uiState.isAssistantTyping
            ) {
                item(
                    key = "assessment-indicator"
                ) {
                    AssessmentIndicator()
                }
            }
        }
    }
}

@Composable
private fun AssessmentIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 4.dp,
                vertical = 2.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(DeutschLime)
        )

        Text(
            text =
                "DeutschCraft aktualisiert deinen Fortschritt …",
            color = DeutschTextSecondary,
            style =
                MaterialTheme.typography
                    .bodySmall
        )
    }
}

@Composable
private fun EmptyConversationContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(DeutschLime),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "D",
                color = DeutschTextOnLime,
                style =
                    MaterialTheme.typography
                        .headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Beginne eine Unterhaltung",
            color = DeutschTextPrimary,
            style =
                MaterialTheme.typography
                    .titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text =
                "Schreibe eine Nachricht auf Deutsch. " +
                        "DeutschCraft antwortet, schlägt passende " +
                        "Antworten vor und verfolgt deinen Fortschritt.",
            color = DeutschTextSecondary,
            style =
                MaterialTheme.typography
                    .bodyMedium
        )
    }
}

private fun assistantStatusText(
    isAssistantTyping: Boolean,
    isAssessing: Boolean
): String {
    return when {
        isAssistantTyping ->
            "schreibt gerade …"

        isAssessing ->
            "analysiert deine Nachricht …"

        else ->
            "AI-Sprachpartner · online"
    }
}

private fun assistantStatusColor(
    isAssistantTyping: Boolean,
    isAssessing: Boolean
): androidx.compose.ui.graphics.Color {
    return if (
        isAssistantTyping ||
        isAssessing
    ) {
        DeutschLime
    } else {
        DeutschTextSecondary
    }
}