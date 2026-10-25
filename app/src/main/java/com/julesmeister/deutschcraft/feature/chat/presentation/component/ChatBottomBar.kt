package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatUiAction
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatUiState

@Composable
fun ChatBottomBar(
    uiState: ChatUiState,
    onAction: (ChatUiAction) -> Unit,
    onAddClick: () -> Unit,
    onInputFocused: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color =
                    MaterialTheme.colorScheme.background
            )
            .navigationBarsPadding()
            .imePadding()
    ) {
        ChatSuggestionSection(
            suggestions =
                uiState.suggestions,
            onSuggestionClick = { suggestionId ->
                onAction(
                    ChatUiAction.SuggestionSelected(
                        suggestionId =
                            suggestionId
                    )
                )

                onInputFocused()
            }
        )

        ChatInputBar(
            value =
                uiState.messageInput,
            isSending =
                uiState.isSending,
            enabled =
                !uiState.isInitializing,
            onValueChange = { value ->
                onAction(
                    ChatUiAction.MessageInputChanged(
                        value =
                            value
                    )
                )
            },
            onSendClick = {
                onInputFocused()

                onAction(
                    ChatUiAction.SendMessage
                )
            },
            onAddClick =
                onAddClick,
            onInputFocused =
                onInputFocused,
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}