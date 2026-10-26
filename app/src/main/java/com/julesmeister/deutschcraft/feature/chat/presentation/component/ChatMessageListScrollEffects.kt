package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.delay

@Composable
internal fun ChatMessageListScrollEffects(
    listState: LazyListState,
    messageCount: Int,
    suggestionCount: Int,
    isSending: Boolean,
    hasTypingIndicator: Boolean,
    keyboardBottom: Int,
    bottomAnchorIndex: Int,
    scrollRequestKey: Int
) {
    /*
     * Initialize this with the current count.
     *
     * Saved messages already present when the chat opens
     * must not be treated as newly received messages.
     */
    val previousMessageCount =
        remember {
            mutableIntStateOf(
                messageCount
            )
        }

    FocusScrollEffect(
        listState =
            listState,
        messageCount =
            messageCount,
        bottomAnchorIndex =
            bottomAnchorIndex,
        scrollRequestKey =
            scrollRequestKey
    )

    KeyboardScrollEffect(
        listState =
            listState,
        messageCount =
            messageCount,
        keyboardBottom =
            keyboardBottom,
        bottomAnchorIndex =
            bottomAnchorIndex
    )

    NewMessageScrollEffect(
        listState =
            listState,
        messageCount =
            messageCount,
        previousMessageCount =
            previousMessageCount.intValue,
        bottomAnchorIndex =
            bottomAnchorIndex,
        onMessageCountHandled = { handledCount ->
            previousMessageCount.intValue =
                handledCount
        }
    )

    TypingIndicatorScrollEffect(
        listState =
            listState,
        messageCount =
            messageCount,
        hasTypingIndicator =
            hasTypingIndicator,
        bottomAnchorIndex =
            bottomAnchorIndex
    )

    /*
     * These values remain in the contract for compatibility.
     *
     * Suggestion changes alter the measured overlay height,
     * but should not independently animate the entire list.
     */
    @Suppress("UNUSED_VARIABLE")
    val ignoredSuggestionCount =
        suggestionCount

    @Suppress("UNUSED_VARIABLE")
    val ignoredSendingState =
        isSending
}

@Composable
private fun FocusScrollEffect(
    listState: LazyListState,
    messageCount: Int,
    bottomAnchorIndex: Int,
    scrollRequestKey: Int
) {
    LaunchedEffect(
        scrollRequestKey
    ) {
        if (
            scrollRequestKey <= 0 ||
            messageCount <= 0
        ) {
            return@LaunchedEffect
        }

        awaitChatLayoutFrames()

        animateToBottomSafely(
            listState =
                listState,
            bottomAnchorIndex =
                bottomAnchorIndex
        )
    }
}

@Composable
private fun KeyboardScrollEffect(
    listState: LazyListState,
    messageCount: Int,
    keyboardBottom: Int,
    bottomAnchorIndex: Int
) {
    LaunchedEffect(
        keyboardBottom
    ) {
        if (
            keyboardBottom <= 0 ||
            messageCount <= 0
        ) {
            return@LaunchedEffect
        }

        delay(
            KEYBOARD_SCROLL_DELAY_MILLIS
        )

        awaitChatLayoutFrames()

        animateToBottomSafely(
            listState =
                listState,
            bottomAnchorIndex =
                bottomAnchorIndex
        )
    }
}

@Composable
private fun NewMessageScrollEffect(
    listState: LazyListState,
    messageCount: Int,
    previousMessageCount: Int,
    bottomAnchorIndex: Int,
    onMessageCountHandled: (Int) -> Unit
) {
    LaunchedEffect(
        messageCount
    ) {
        if (
            messageCount <=
            previousMessageCount
        ) {
            onMessageCountHandled(
                messageCount
            )

            return@LaunchedEffect
        }

        delay(
            CONTENT_SCROLL_DELAY_MILLIS
        )

        awaitChatLayoutFrames()

        animateToBottomSafely(
            listState =
                listState,
            bottomAnchorIndex =
                bottomAnchorIndex
        )

        onMessageCountHandled(
            messageCount
        )
    }
}

@Composable
private fun TypingIndicatorScrollEffect(
    listState: LazyListState,
    messageCount: Int,
    hasTypingIndicator: Boolean,
    bottomAnchorIndex: Int
) {
    LaunchedEffect(
        hasTypingIndicator
    ) {
        if (
            messageCount <= 0 ||
            !hasTypingIndicator
        ) {
            return@LaunchedEffect
        }

        awaitChatLayoutFrames()

        animateToBottomSafely(
            listState =
                listState,
            bottomAnchorIndex =
                bottomAnchorIndex
        )
    }
}

private suspend fun animateToBottomSafely(
    listState: LazyListState,
    bottomAnchorIndex: Int
) {
    val totalItemCount =
        listState
            .layoutInfo
            .totalItemsCount

    if (totalItemCount <= 0) {
        return
    }

    val safeIndex =
        bottomAnchorIndex.coerceIn(
            minimumValue =
                0,
            maximumValue =
                totalItemCount - 1
        )

    listState.animateScrollToItem(
        index =
            safeIndex
    )
}

private suspend fun awaitChatLayoutFrames() {
    withFrameNanos {
        // Wait for state to enter composition.
    }

    withFrameNanos {
        // Wait for LazyColumn measurement.
    }
}