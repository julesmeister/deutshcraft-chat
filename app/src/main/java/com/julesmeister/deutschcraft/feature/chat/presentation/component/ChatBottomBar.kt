package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatUiAction
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatUiState

/*
 * onAddClick is intentionally retained.
 *
 * The current Telegram-style composer does not display an
 * add button, but this callback remains available for a
 * future attachment or learning-tools action.
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
    /*
     * There is deliberately no background on this Column.
     *
     * The conversation remains visible through the spaces:
     *
     * - Around the suggestion pills
     * - Between suggestions and composer
     * - To the left and right of the composer
     */
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(
                start =
                    BOTTOM_BAR_HORIZONTAL_PADDING,
                end =
                    BOTTOM_BAR_HORIZONTAL_PADDING,
                top =
                    BOTTOM_BAR_TOP_PADDING,
                bottom =
                    BOTTOM_BAR_BOTTOM_PADDING
            )
    ) {
        FloatingReplySuggestions(
            suggestions =
                uiState.suggestions,
            enabled =
                !uiState.isInitializing &&
                        !uiState.isSending,
            onSuggestionClick = {
                    suggestionId ->

                onAction(
                    ChatUiAction
                        .SuggestionSelected(
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
                    Modifier.height(
                        SUGGESTION_COMPOSER_SPACING
                    )
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
                    ChatUiAction
                        .MessageInputChanged(
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

private val BOTTOM_BAR_HORIZONTAL_PADDING =
    12.dp

private val BOTTOM_BAR_TOP_PADDING =
    4.dp

private val BOTTOM_BAR_BOTTOM_PADDING =
    10.dp

private val SUGGESTION_COMPOSER_SPACING =
    8.dp