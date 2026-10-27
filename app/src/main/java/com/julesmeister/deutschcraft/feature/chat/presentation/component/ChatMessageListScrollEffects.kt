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
     * Initialize with the current message count.
     *
     * Messages already loaded from Room when opening the
     * conversation must not be treated as newly-arrived
     * messages.
     */
    val previousMessageCount =
        remember {
            mutableIntStateOf(
                messageCount
            )
        }

    /*
     * Same idea for reply suggestions.
     *
     * If cached suggestions already exist when opening the
     * conversation, don't treat them as newly appearing.
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
     * Reply suggestions can appear after the assistant
     * message has already been scrolled into view.
     *
     * Because the suggestion row lives above the composer,
     * its appearance increases the bottom overlay height and
     * reduces the visible LazyColumn viewport.
     *
     * Scroll again after that layout settles.
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
     * Sending remains in the contract because other chat UI
     * state may rely on it later.
     *
     * It does not independently trigger scrolling.
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
         * Give the newly-created message and its entrance
         * animation a moment to enter composition.
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
         * Suggestions disappeared or the conversation is
         * empty.
         *
         * Remember the new count but don't force a scroll.
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
         * Only respond when suggestions have actually appeared
         * or increased.
         *
         * Example:
         *
         * 0 -> 3
         *
         * triggers the second bottom scroll.
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
         * The suggestion pills are animated above the composer.
         *
         * Waiting briefly is important because scrolling on
         * the exact state update can happen before their new
         * height has affected the available chat viewport.
         */
        delay(
            SUGGESTION_SCROLL_DELAY_MILLIS
        )

        /*
         * Wait for Compose measurement after the delay.
         */
        awaitChatLayoutFrames()

        animateToBottomSafely(
            listState =
                listState,
            bottomAnchorIndex =
                bottomAnchorIndex
        )

        /*
         * One extra frame lets the bottom overlay's
         * onSizeChanged update settle.
         *
         * Then perform a final correction. This is intentional
         * because the floating suggestions can alter the
         * viewport after the first scroll has begun.
         */
        withFrameNanos {
            // Wait for updated overlay measurement.
        }

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
        /*
         * Wait for the changed state to enter composition.
         */
    }

    withFrameNanos {
        /*
         * Wait for LazyColumn and overlay measurement.
         */
    }
}

/*
 * Suggestions animate above the composer, so this intentionally
 * waits longer than an ordinary composition frame.
 *
 * 160 ms is long enough for the suggestion row to affect the
 * measured bottom overlay while still feeling immediate.
 */
private const val SUGGESTION_SCROLL_DELAY_MILLIS =
    160L