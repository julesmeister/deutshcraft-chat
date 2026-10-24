package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatUiState
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@Composable
fun ChatMessageList(
    uiState: ChatUiState,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val displayedItemCount =
        uiState.messages.size +
                if (uiState.isAssistantTyping) {
                    1
                } else {
                    0
                } +
                if (
                    uiState.isAssessing &&
                    !uiState.isAssistantTyping
                ) {
                    1
                } else {
                    0
                }

    LaunchedEffect(displayedItemCount) {
        if (displayedItemCount > 0) {
            listState.animateScrollToItem(
                index = displayedItemCount - 1
            )
        }
    }

    Box(
        modifier = modifier
    ) {
        if (uiState.isInitializing) {
            CircularProgressIndicator(
                modifier = Modifier.align(
                    Alignment.Center
                ),
                color = DeutschLime,
                strokeWidth = 3.dp
            )
        } else if (
            uiState.isConversationEmpty &&
            !uiState.isAssistantTyping
        ) {
            ChatEmptyState(
                modifier = Modifier.align(
                    Alignment.Center
                )
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background
                ),
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
                    key = TYPING_INDICATOR_KEY
                ) {
                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        TypingIndicator()

                        Spacer(
                            modifier =
                                Modifier.weight(1f)
                        )
                    }
                }
            }

            if (
                uiState.isAssessing &&
                !uiState.isAssistantTyping
            ) {
                item(
                    key =
                        ASSESSMENT_INDICATOR_KEY
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
        verticalAlignment =
            Alignment.CenterVertically,
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
                "DeutschCraft aktualisiert " +
                        "deinen Fortschritt …",
            color = DeutschTextSecondary,
            style =
                MaterialTheme.typography
                    .bodySmall
        )
    }
}

private const val TYPING_INDICATOR_KEY =
    "typing-indicator"

private const val ASSESSMENT_INDICATOR_KEY =
    "assessment-indicator"