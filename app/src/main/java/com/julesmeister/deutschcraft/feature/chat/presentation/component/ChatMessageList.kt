package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatMessageList(
    uiState: ChatUiState,
    onMessageClick: (ChatMessage) -> Unit,
    modifier: Modifier = Modifier,
    scrollRequestKey: Int = 0,
    bottomOverlayHeight: Dp = 0.dp
) {
    val messageCount =
        uiState.messages.size

    val hasTypingIndicator =
        uiState.isAssistantTyping

    /*
     * Do not create LazyListState while the conversation is
     * still empty.
     *
     * Room usually emits an empty state first and the saved
     * messages afterward. Creating the state during that
     * empty emission permanently initializes it at index 0.
     */
    if (
        messageCount == 0 &&
        !hasTypingIndicator
    ) {
        ChatMessageListEmptyState(
            modifier =
                modifier
        )

        return
    }

    val suggestionCount =
        uiState.suggestions.size

    /*
     * LazyColumn contains:
     *
     * messageCount message items
     * + optional typing indicator
     * + one bottom-anchor item
     *
     * Therefore this value is the index of the bottom
     * anchor.
     */
    val bottomAnchorIndex =
        calculateBottomAnchorIndex(
            messageCount =
                messageCount,
            hasTypingIndicator =
                hasTypingIndicator
        )

    /*
     * This state is now created only after messages exist.
     *
     * The conversation therefore opens directly at the
     * bottom anchor instead of rendering index 0 first and
     * scrolling afterward.
     */
    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex =
                bottomAnchorIndex
        )

    val density =
        LocalDensity.current

    val keyboardBottom =
        WindowInsets.ime.getBottom(
            density =
                density
        )

    val latestUserMessageId =
        remember(
            uiState.messages
        ) {
            uiState.messages
                .lastOrNull { message ->
                    message.sender ==
                            MessageSender.USER
                }
                ?.id
        }

    val latestAssistantMessageId =
        remember(
            uiState.messages
        ) {
            uiState.messages
                .lastOrNull { message ->
                    message.sender ==
                            MessageSender.ASSISTANT
                }
                ?.id
        }

    /*
     * Existing messages should not replay their entrance
     * animations when the conversation opens.
     */
    val seenMessageIds =
        remember {
            mutableStateMapOf<String, Boolean>()
                .apply {
                    uiState.messages.forEach { message ->
                        put(
                            message.id,
                            true
                        )
                    }
                }
        }

    ChatMessageListScrollEffects(
        listState =
            listState,
        messageCount =
            messageCount,
        suggestionCount =
            suggestionCount,
        isSending =
            uiState.isSending,
        hasTypingIndicator =
            hasTypingIndicator,
        keyboardBottom =
            keyboardBottom,
        bottomAnchorIndex =
            bottomAnchorIndex,
        scrollRequestKey =
            scrollRequestKey
    )

    LazyColumn(
        modifier =
            modifier.fillMaxSize(),
        state =
            listState,
        reverseLayout =
            false,
        contentPadding =
            PaddingValues(
                top =
                    MESSAGE_LIST_TOP_PADDING,
                bottom =
                    bottomOverlayHeight +
                            MESSAGE_LIST_BOTTOM_BREATHING_ROOM
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                MESSAGE_SPACING
            )
    ) {
        items(
            items =
                uiState.messages,
            key = { message ->
                message.id
            }
        ) { message ->
            AnimatedMessageItem(
                message =
                    message,
                correction =
                    message.correction,
                isLatestUserMessage =
                    message.id ==
                            latestUserMessageId,
                isLatestAssistantMessage =
                    message.id ==
                            latestAssistantMessageId,
                hasAlreadyBeenSeen =
                    seenMessageIds[
                        message.id
                    ] == true,
                onAnimationStarted = {
                    seenMessageIds[
                        message.id
                    ] = true
                },
                onClick = {
                    onMessageClick(
                        message
                    )
                },
                modifier =
                    Modifier.padding(
                        horizontal =
                            MESSAGE_HORIZONTAL_PADDING,
                        vertical =
                            MESSAGE_VERTICAL_PADDING
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
                            horizontal =
                                MESSAGE_HORIZONTAL_PADDING,
                            vertical =
                                TYPING_INDICATOR_VERTICAL_PADDING
                        )
                )
            }
        }

        /*
         * Stable final item used as the bottom scrolling
         * target.
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

@Composable
private fun ChatMessageListEmptyState(
    modifier: Modifier
) {
    Box(
        modifier =
            modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {
        ChatEmptyState()
    }
}

private fun calculateBottomAnchorIndex(
    messageCount: Int,
    hasTypingIndicator: Boolean
): Int {
    return messageCount +
            if (hasTypingIndicator) {
                1
            } else {
                0
            }
}

private val MESSAGE_LIST_TOP_PADDING =
    8.dp

private val MESSAGE_LIST_BOTTOM_BREATHING_ROOM =
    6.dp

private val MESSAGE_SPACING =
    6.dp

private val MESSAGE_HORIZONTAL_PADDING =
    12.dp

private val MESSAGE_VERTICAL_PADDING =
    2.dp

private val TYPING_INDICATOR_VERTICAL_PADDING =
    4.dp