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

    val bottomAnchorIndex =
        calculateBottomAnchorIndex(
            messageCount =
                messageCount,
            hasTypingIndicator =
                hasTypingIndicator
        )

    val latestUserMessageId =
        uiState.messages
            .latestMessageId(
                sender =
                    MessageSender.USER
            )

    val latestAssistantMessageId =
        uiState.messages
            .latestMessageId(
                sender =
                    MessageSender.ASSISTANT
            )

    /*
     * Messages already present when this list first opens
     * should not replay their entrance animations.
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

    if (
        uiState.messages.isEmpty() &&
        !hasTypingIndicator
    ) {
        ChatMessageListEmptyState(
            modifier =
                modifier
        )

        return
    }

    LazyColumn(
        modifier =
            modifier.fillMaxSize(),
        state =
            listState,
        reverseLayout =
            false,

        /*
         * The list receives exactly the measured floating
         * bottom-bar height.
         *
         * This does not paint a black panel. It only gives
         * the final message enough scroll range to move
         * above the floating composer and suggestions.
         */
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
         * Retained as a tiny stable scrolling target.
         *
         * The measured content padding handles composer
         * clearance. This anchor remains only for existing
         * scroll calculations.
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

private fun List<ChatMessage>.latestMessageId(
    sender: MessageSender
): String? {
    return lastOrNull { message ->
        message.sender ==
                sender
    }?.id
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