package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
internal fun ChatMessageListScrollEffects(
    listState: LazyListState,
    messageCount: Int,
    suggestionCount: Int,
    isSending: Boolean,
    hasTypingIndicator: Boolean,
    keyboardBottom: Int,

    /*
     * Actual measured height of ChatBottomBar, including:
     *
     * - composer
     * - reply suggestions
     * - any additional floating bottom content
     *
     * This is the authoritative signal that the visible chat
     * viewport has become smaller.
     */
    bottomOverlayHeight: Dp,

    bottomAnchorIndex: Int,
    scrollRequestKey: Int
) {
    /*
     * Initialize with the current count.
     *
     * Saved messages already present when the conversation
     * opens must not be mistaken for newly-arrived messages.
     */
    val previousMessageCount =
        remember {
            mutableIntStateOf(
                messageCount
            )
        }

    /*
     * Suggestions may load after the conversation opens.
     *
     * Tracking their previous count lets us react specifically
     * when suggestions appear or increase.
     */
    val previousSuggestionCount =
        remember {
            mutableIntStateOf(
                suggestionCount
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
     * Early scroll when suggestions appear.
     *
     * This reacts to the data becoming available.
     */
    SuggestionScrollEffect(
        listState =
            listState,
        messageCount =
            messageCount,
        suggestionCount =
            suggestionCount,
        previousSuggestionCount =
            previousSuggestionCount.intValue,
        bottomAnchorIndex =
            bottomAnchorIndex,
        onSuggestionCountHandled = { handledCount ->
            previousSuggestionCount.intValue =
                handledCount
        }
    )

    /*
     * Final authoritative correction.
     *
     * This reacts after ChatBottomBar has actually measured
     * its new height. It covers:
     *
     * - suggestions appearing during an active chat
     * - suggestions loading after reopening a conversation
     * - suggestion animations changing the overlay height
     * - keyboard and composer layout adjustments
     */
    BottomOverlayScrollEffect(
        listState =
            listState,
        messageCount =
            messageCount,
        bottomOverlayHeight =
            bottomOverlayHeight,
        bottomAnchorIndex =
            bottomAnchorIndex
    )

    /*
     * Keep isSending in the function contract.
     *
     * Sending alone does not change the visible viewport, so
     * it does not independently start a scroll animation.
     */
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
        /*
         * Covers message removal and the initial state.
         */
        if (
            messageCount <=
            previousMessageCount
        ) {
            onMessageCountHandled(
                messageCount
            )

            return@LaunchedEffect
        }

        /*
         * Let the message enter composition and begin its
         * entrance animation.
         */
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

@Composable
private fun SuggestionScrollEffect(
    listState: LazyListState,
    messageCount: Int,
    suggestionCount: Int,
    previousSuggestionCount: Int,
    bottomAnchorIndex: Int,
    onSuggestionCountHandled: (Int) -> Unit
) {
    LaunchedEffect(
        suggestionCount
    ) {
        /*
         * Suggestions disappeared or no messages exist.
         *
         * Update the stored count without forcing a scroll.
         */
        if (
            suggestionCount <= 0 ||
            messageCount <= 0
        ) {
            onSuggestionCountHandled(
                suggestionCount
            )

            return@LaunchedEffect
        }

        /*
         * Ignore unchanged or reduced suggestion counts.
         */
        if (
            suggestionCount <=
            previousSuggestionCount
        ) {
            onSuggestionCountHandled(
                suggestionCount
            )

            return@LaunchedEffect
        }

        /*
         * Suggestions normally animate above the composer.
         *
         * This delay gives their visibility transition time to
         * start affecting the layout.
         */
        delay(
            SUGGESTION_SCROLL_DELAY_MILLIS
        )

        awaitChatLayoutFrames()

        animateToBottomSafely(
            listState =
                listState,
            bottomAnchorIndex =
                bottomAnchorIndex
        )

        onSuggestionCountHandled(
            suggestionCount
        )
    }
}

@Composable
private fun BottomOverlayScrollEffect(
    listState: LazyListState,
    messageCount: Int,
    bottomOverlayHeight: Dp,
    bottomAnchorIndex: Int
) {
    LaunchedEffect(
        bottomOverlayHeight
    ) {
        if (
            messageCount <= 0 ||
            bottomOverlayHeight <= 0.dp
        ) {
            return@LaunchedEffect
        }

        /*
         * onSizeChanged has reported the new bottom-bar
         * measurement.
         *
         * Wait for ChatMessageList to receive the new bottom
         * padding and for LazyColumn to recalculate its
         * viewport.
         */
        awaitChatLayoutFrames()

        delay(
            OVERLAY_SCROLL_SETTLE_DELAY_MILLIS
        )

        /*
         * First movement follows the new measured viewport.
         */
        animateToBottomSafely(
            listState =
                listState,
            bottomAnchorIndex =
                bottomAnchorIndex
        )

        /*
         * Suggestion visibility may still be finishing its
         * animation. Wait for two more layout frames before
         * making the final correction.
         */
        awaitChatLayoutFrames()

        /*
         * Use an immediate final correction.
         *
         * This avoids an assistant bubble remaining partially
         * hidden behind the suggestion row after reopening a
         * conversation.
         */
        scrollToBottomSafely(
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
    val safeIndex =
        listState.safeBottomIndex(
            requestedIndex =
                bottomAnchorIndex
        ) ?: return

    listState.animateScrollToItem(
        index =
            safeIndex
    )
}

private suspend fun scrollToBottomSafely(
    listState: LazyListState,
    bottomAnchorIndex: Int
) {
    val safeIndex =
        listState.safeBottomIndex(
            requestedIndex =
                bottomAnchorIndex
        ) ?: return

    listState.scrollToItem(
        index =
            safeIndex
    )
}

private fun LazyListState.safeBottomIndex(
    requestedIndex: Int
): Int? {
    val totalItemCount =
        layoutInfo.totalItemsCount

    if (totalItemCount <= 0) {
        return null
    }

    return requestedIndex.coerceIn(
        minimumValue =
            0,
        maximumValue =
            totalItemCount - 1
    )
}

private suspend fun awaitChatLayoutFrames() {
    withFrameNanos {
        /*
         * Wait for changed state to enter composition.
         */
    }

    withFrameNanos {
        /*
         * Wait for LazyColumn and bottom-overlay measurement.
         */
    }
}

/*
 * Allows suggestion chips to enter their visibility animation
 * before the early scroll.
 */
private const val SUGGESTION_SCROLL_DELAY_MILLIS =
    160L

/*
 * Allows the updated bottom content padding to reach the
 * LazyColumn before scrolling.
 */
private const val OVERLAY_SCROLL_SETTLE_DELAY_MILLIS =
    80L