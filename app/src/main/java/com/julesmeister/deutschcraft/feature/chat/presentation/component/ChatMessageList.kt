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
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
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
     * The bottom anchor appears after all messages and
     * after the optional typing indicator.
     */
    val bottomAnchorIndex =
        messageCount +
                if (hasTypingIndicator) {
                    1
                } else {
                    0
                }

    /*
     * Identify the most recent learner message.
     *
     * Only this user bubble receives the mint-green
     * background.
     */
    val latestUserMessageId =
        uiState.messages
            .lastOrNull { message ->
                message.sender ==
                        MessageSender.USER
            }
            ?.id

    /*
     * Identify the most recent assistant message.
     *
     * Only this assistant bubble receives the subtle
     * background-color pulse.
     */
    val latestAssistantMessageId =
        uiState.messages
            .lastOrNull { message ->
                message.sender ==
                        MessageSender.ASSISTANT
            }
            ?.id

    /*
     * Scroll when the actual composer receives focus.
     *
     * Two layout frames are allowed before starting the
     * animation so Compose can begin applying the updated
     * layout measurements.
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
     * Scroll again after the keyboard changes the
     * available chat height.
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
     * Keep the latest content visible when:
     *
     * - A user message is added
     * - Sending begins
     * - The typing indicator appears or disappears
     * - The assistant message arrives
     * - Suggested replies appear
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
            modifier =
                modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {
            ChatEmptyState()
        }

        return
    }

    LazyColumn(
        modifier =
            modifier.fillMaxSize(),
        state =
            listState,
        reverseLayout =
            false,
        verticalArrangement =
            Arrangement.spacedBy(6.dp)
    ) {
        items(
            items =
                uiState.messages,
            key = { message ->
                message.id
            }
        ) { message ->
            MessageBubble(
                message =
                    message,

                /*
                 * Besser is persisted in Room and restored
                 * as part of ChatMessage.
                 */
                correction =
                    message.correction,

                isLatestUserMessage =
                    message.id ==
                            latestUserMessageId,

                isLatestAssistantMessage =
                    message.id ==
                            latestAssistantMessageId,

                onClick = {
                    onMessageClick(message)
                },

                modifier =
                    Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 2.dp
                    )
            )
        }

        if (hasTypingIndicator) {
            item(
                key =
                    TYPING_INDICATOR_KEY
            ) {
                TypingIndicator(
                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 4.dp
                        )
                )
            }
        }

        /*
         * A real item after the final message gives the
         * list a reliable scrolling target when the
         * keyboard or suggestion section changes height.
         */
        item(
            key =
                BOTTOM_ANCHOR_KEY
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        BOTTOM_ANCHOR_HEIGHT
                    )
            )
        }
    }
}

private suspend fun awaitLayoutFrames() {
    withFrameNanos {
        // Wait for the focus update.
    }

    withFrameNanos {
        // Wait for the resized layout measurement.
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
    140L