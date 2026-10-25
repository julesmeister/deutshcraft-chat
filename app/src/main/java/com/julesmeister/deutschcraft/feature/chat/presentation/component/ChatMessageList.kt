package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatUiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatMessageList(
    uiState: ChatUiState,
    onMessageClick: (ChatMessage) -> Unit,
    modifier: Modifier = Modifier,
    scrollRequestKey: Int = 0
) {
    val listState =
        rememberLazyListState()

    val density =
        LocalDensity.current

    /*
     * This value changes while the keyboard opens and
     * closes. It lets the list react after the keyboard
     * changes the available screen height.
     */
    val keyboardBottom =
        WindowInsets.ime.getBottom(
            density = density
        )

    val messageCount =
        uiState.messages.size

    val suggestionCount =
        uiState.suggestions.size

    val hasTypingIndicator =
        uiState.isAssistantTyping

    /*
     * The anchor appears after all messages and after the
     * optional typing indicator.
     *
     * Examples:
     *
     * 4 messages, no typing:
     * Bottom anchor index = 4
     *
     * 4 messages, typing indicator:
     * Bottom anchor index = 5
     */
    val bottomAnchorIndex =
        messageCount +
                if (hasTypingIndicator) {
                    1
                } else {
                    0
                }

    /*
     * Runs when ChatInputBar reports that its actual
     * text field has gained focus.
     *
     * Waiting for two frames gives Compose time to begin
     * recalculating the keyboard-adjusted screen layout.
     */
    LaunchedEffect(scrollRequestKey) {
        if (
            scrollRequestKey <= 0 ||
            messageCount <= 0
        ) {
            return@LaunchedEffect
        }

        awaitLayoutFrames()

        listState.animateScrollToItem(
            index = bottomAnchorIndex
        )
    }

    /*
     * Runs as the keyboard inset changes.
     *
     * LaunchedEffect automatically cancels the previous
     * pending execution when keyboardBottom changes again.
     * Therefore, only the latest keyboard position should
     * produce the final animated scroll.
     */
    LaunchedEffect(keyboardBottom) {
        if (
            keyboardBottom <= 0 ||
            messageCount <= 0
        ) {
            return@LaunchedEffect
        }

        delay(
            KEYBOARD_SCROLL_DELAY_MILLIS
        )

        listState.animateScrollToItem(
            index = bottomAnchorIndex
        )
    }

    /*
     * Scroll when conversation content changes:
     *
     * - User message is inserted
     * - Sending starts
     * - Assistant typing indicator appears
     * - Assistant response arrives
     * - AI suggestions arrive
     */
    LaunchedEffect(
        messageCount,
        uiState.isSending,
        hasTypingIndicator,
        suggestionCount
    ) {
        if (messageCount <= 0) {
            return@LaunchedEffect
        }

        delay(
            CONTENT_SCROLL_DELAY_MILLIS
        )

        listState.animateScrollToItem(
            index = bottomAnchorIndex
        )
    }

    if (
        uiState.messages.isEmpty() &&
        !hasTypingIndicator
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ChatEmptyState()
        }

        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        reverseLayout = false,
        verticalArrangement =
            Arrangement.spacedBy(6.dp)
    ) {
        items(
            items = uiState.messages,
            key = { message ->
                message.id
            }
        ) { message ->
            MessageBubble(
                message = message,
                correction =
                    uiState.corrections[message.id],
                onClick = {
                    onMessageClick(message)
                },
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 2.dp
                )
            )
        }

        if (hasTypingIndicator) {
            item(
                key = TYPING_INDICATOR_KEY
            ) {
                TypingIndicator(
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 4.dp
                    )
                )
            }
        }

        /*
         * This real list item represents the true bottom.
         *
         * Scrolling to the final message alone might do
         * nothing if Compose considers part of that message
         * visible. Scrolling to this anchor forces the end
         * of the list into view.
         */
        item(
            key = BOTTOM_ANCHOR_KEY
        ) {
            Spacer(
                modifier = Modifier.height(
                    BOTTOM_ANCHOR_HEIGHT
                )
            )
        }
    }
}

private suspend fun awaitLayoutFrames() {
    withFrameNanos {
        // Wait for focus to enter composition.
    }

    withFrameNanos {
        // Wait for the resized layout to be measured.
    }
}

private const val TYPING_INDICATOR_KEY =
    "assistant-typing-indicator"

private const val BOTTOM_ANCHOR_KEY =
    "chat-bottom-anchor"

private val BOTTOM_ANCHOR_HEIGHT =
    8.dp

private const val KEYBOARD_SCROLL_DELAY_MILLIS =
    140L

private const val CONTENT_SCROLL_DELAY_MILLIS =
    100L