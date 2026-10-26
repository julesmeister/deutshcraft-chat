package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatUiAction
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatUiState

/*
 * onAddClick remains temporarily for compatibility with
 * the existing ChatScreen call.
 *
 * The composer no longer displays an attachment button.
 */
@Suppress("UNUSED_PARAMETER")
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
            .windowInsetsPadding(
                WindowInsets.navigationBars
            )
            .windowInsetsPadding(
                WindowInsets.ime
            )
            .padding(
                start = 12.dp,
                end = 12.dp,
                bottom = 10.dp
            )
    ) {
        FloatingReplySuggestions(
            suggestions =
                uiState.suggestions,
            enabled =
                !uiState.isInitializing &&
                        !uiState.isSending,
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

        if (uiState.suggestions.isNotEmpty()) {
            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )
        }

        ChatComposer(
            value =
                uiState.messageInput,
            enabled =
                !uiState.isInitializing,
            isSending =
                uiState.isSending,
            onValueChange = { value ->
                onAction(
                    ChatUiAction.MessageInputChanged(
                        value =
                            value
                    )
                )
            },
            onSendClick = {
                onAction(
                    ChatUiAction.SendMessage
                )
            },
            onInputFocused =
                onInputFocused,
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}