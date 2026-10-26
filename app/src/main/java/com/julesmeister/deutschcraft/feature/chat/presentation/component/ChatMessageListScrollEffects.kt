package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

    ContentScrollEffect(
        listState =
            listState,
        messageCount =
            messageCount,
        suggestionCount =
            suggestionCount,
        isSending =
            isSending,
        hasTypingIndicator =
            hasTypingIndicator,
        bottomAnchorIndex =
            bottomAnchorIndex
    )
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

        scrollToBottomSafely(
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

        scrollToBottomSafely(
            listState =
                listState,
            bottomAnchorIndex =
                bottomAnchorIndex
        )
    }
}

@Composable
private fun ContentScrollEffect(
    listState: LazyListState,
    messageCount: Int,
    suggestionCount: Int,
    isSending: Boolean,
    hasTypingIndicator: Boolean,
    bottomAnchorIndex: Int
) {
    LaunchedEffect(
        messageCount,
        suggestionCount,
        isSending,
        hasTypingIndicator
    ) {
        if (messageCount <= 0) {
            return@LaunchedEffect
        }

        delay(
            CONTENT_SCROLL_DELAY_MILLIS
        )

        scrollToBottomSafely(
            listState =
                listState,
            bottomAnchorIndex =
                bottomAnchorIndex
        )
    }
}

private suspend fun scrollToBottomSafely(
    listState: LazyListState,
    bottomAnchorIndex: Int
) {
    val totalItemCount =
        listState.layoutInfo
            .totalItemsCount

    if (totalItemCount <= 0) {
        return
    }

    val safeIndex =
        bottomAnchorIndex.coerceIn(
            minimumValue = 0,
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
        // Wait for focus state to enter composition.
    }

    withFrameNanos {
        // Wait for the resized layout measurement.
    }
}