package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ChatInputBar
import com.julesmeister.deutschcraft.feature.chat.presentation.component.MessageBubble
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ReplySuggestions
import com.julesmeister.deutschcraft.feature.chat.presentation.component.TypingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onAction: (ChatUiAction) -> Unit,
    onProgressClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val displayedItemCount =
        uiState.messages.size +
                if (uiState.isAssistantTyping) 1 else 0

    LaunchedEffect(displayedItemCount) {
        if (displayedItemCount > 0) {
            listState.animateScrollToItem(
                displayedItemCount - 1
            )
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage
            ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(errorMessage)
        onAction(ChatUiAction.ErrorDismissed)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "DeutschCraft")

                        Text(
                            text = if (uiState.isAssistantTyping) {
                                "schreibt ..."
                            } else {
                                "online"
                            }
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onProgressClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "View German progress"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                ReplySuggestions(
                    suggestions = uiState.suggestions,
                    onSuggestionClick = { suggestionId ->
                        onAction(
                            ChatUiAction.SuggestionSelected(
                                suggestionId
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
                                value
                            )
                        )
                    },
                    onSendClick = {
                        onAction(ChatUiAction.SendMessage)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 12.dp,
                    vertical = 12.dp
                )
            ) {
                items(
                    items = uiState.messages,
                    key = { message ->
                        message.id
                    }
                ) { message ->
                    MessageBubble(message = message)
                }

                if (uiState.isAssistantTyping) {
                    item(key = "typing-indicator") {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TypingIndicator()
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}