// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/component/ChatMessageListScrollEffects.kt

package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
internal fun ChatMessageListScrollEffects(
    listState: LazyListState,
    messageCount: Int,
    suggestionCount: Int,
    isSending: Boolean,
    hasTypingIndicator: Boolean,
    keyboardBottom: Int,
    bottomOverlayHeight: Dp,
    bottomAnchorIndex: Int,
    scrollRequestKey: Int
) {
    val previousMessageCount =
        remember {
            mutableIntStateOf(
                messageCount
            )
        }

    val previousSuggestionCount =
        remember {
            mutableIntStateOf(
                suggestionCount
            )
        }

    val initialBottomSettled =
        remember {
            mutableStateOf(
                false
            )
        }

    val shouldFollowBottom =
        remember {
            mutableStateOf(
                true
            )
        }

    InitialBottomSettlementEffect(
        listState =
            listState,

        messageCount =
            messageCount,

        bottomOverlayHeight =
            bottomOverlayHeight,

        bottomAnchorIndex =
            bottomAnchorIndex,

        initialBottomSettled =
            initialBottomSettled,

        shouldFollowBottom =
            shouldFollowBottom
    )

    BottomFollowingObservationEffect(
        listState =
            listState,

        initialBottomSettled =
            initialBottomSettled,

        shouldFollowBottom =
            shouldFollowBottom
    )

    FocusScrollEffect(
        listState =
            listState,

        messageCount =
            messageCount,

        bottomAnchorIndex =
            bottomAnchorIndex,

        scrollRequestKey =
            scrollRequestKey,

        shouldFollowBottom =
            shouldFollowBottom
    )

    KeyboardScrollEffect(
        listState =
            listState,

        messageCount =
            messageCount,

        keyboardBottom =
            keyboardBottom,

        bottomAnchorIndex =
            bottomAnchorIndex,

        shouldFollowBottom =
            shouldFollowBottom
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

        shouldFollowBottom =
            shouldFollowBottom,

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
            bottomAnchorIndex,

        shouldFollowBottom =
            shouldFollowBottom
    )

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

        shouldFollowBottom =
            shouldFollowBottom,

        onSuggestionCountHandled = { handledCount ->
            previousSuggestionCount.intValue =
                handledCount
        }
    )

    BottomOverlayScrollEffect(
        listState =
            listState,

        messageCount =
            messageCount,

        bottomOverlayHeight =
            bottomOverlayHeight,

        bottomAnchorIndex =
            bottomAnchorIndex,

        initialBottomSettled =
            initialBottomSettled,

        shouldFollowBottom =
            shouldFollowBottom
    )

    SendingScrollEffect(
        listState =
            listState,

        messageCount =
            messageCount,

        isSending =
            isSending,

        bottomAnchorIndex =
            bottomAnchorIndex,

        shouldFollowBottom =
            shouldFollowBottom
    )
}

@Composable
private fun InitialBottomSettlementEffect(
    listState: LazyListState,
    messageCount: Int,
    bottomOverlayHeight: Dp,
    bottomAnchorIndex: Int,
    initialBottomSettled: MutableState<Boolean>,
    shouldFollowBottom: MutableState<Boolean>
) {
    LaunchedEffect(
        messageCount,
        bottomAnchorIndex,
        bottomOverlayHeight
    ) {
        if (
            initialBottomSettled.value ||
            messageCount <= 0 ||
            bottomOverlayHeight <= 0.dp
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

        delay(
            CHAT_INITIAL_BOTTOM_SETTLE_DELAY_MILLIS
        )

        awaitChatLayoutFrames()

        scrollToBottomSafely(
            listState =
                listState,

            bottomAnchorIndex =
                bottomAnchorIndex
        )

        awaitChatLayoutFrames()

        scrollToBottomSafely(
            listState =
                listState,

            bottomAnchorIndex =
                bottomAnchorIndex
        )

        shouldFollowBottom.value =
            true

        initialBottomSettled.value =
            true
    }
}

@Composable
private fun BottomFollowingObservationEffect(
    listState: LazyListState,
    initialBottomSettled: MutableState<Boolean>,
    shouldFollowBottom: MutableState<Boolean>
) {
    LaunchedEffect(
        listState
    ) {
        snapshotFlow {
            if (
                !initialBottomSettled.value
            ) {
                true
            } else {
                listState.isAtConversationBottom()
            }
        }
            .distinctUntilChanged()
            .collectLatest { isAtBottom ->
                if (
                    initialBottomSettled.value
                ) {
                    shouldFollowBottom.value =
                        isAtBottom
                }
            }
    }
}

@Composable
private fun FocusScrollEffect(
    listState: LazyListState,
    messageCount: Int,
    bottomAnchorIndex: Int,
    scrollRequestKey: Int,
    shouldFollowBottom: MutableState<Boolean>
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

        shouldFollowBottom.value =
            true

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
    bottomAnchorIndex: Int,
    shouldFollowBottom: MutableState<Boolean>
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

        shouldFollowBottom.value =
            true

        delay(
            CHAT_KEYBOARD_SCROLL_DELAY_MILLIS
        )

        awaitChatLayoutFrames()

        animateToBottomSafely(
            listState =
                listState,

            bottomAnchorIndex =
                bottomAnchorIndex
        )

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
private fun NewMessageScrollEffect(
    listState: LazyListState,
    messageCount: Int,
    previousMessageCount: Int,
    bottomAnchorIndex: Int,
    shouldFollowBottom: MutableState<Boolean>,
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

        if (
            !shouldFollowBottom.value
        ) {
            onMessageCountHandled(
                messageCount
            )

            return@LaunchedEffect
        }

        delay(
            CHAT_CONTENT_SCROLL_DELAY_MILLIS
        )

        awaitChatLayoutFrames()

        animateToBottomSafely(
            listState =
                listState,

            bottomAnchorIndex =
                bottomAnchorIndex
        )

        delay(
            CHAT_MESSAGE_FINAL_CORRECTION_DELAY_MILLIS
        )

        awaitChatLayoutFrames()

        if (
            shouldFollowBottom.value
        ) {
            scrollToBottomSafely(
                listState =
                    listState,

                bottomAnchorIndex =
                    bottomAnchorIndex
            )
        }

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
    bottomAnchorIndex: Int,
    shouldFollowBottom: MutableState<Boolean>
) {
    LaunchedEffect(
        hasTypingIndicator
    ) {
        if (
            messageCount <= 0 ||
            !hasTypingIndicator ||
            !shouldFollowBottom.value
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
    shouldFollowBottom: MutableState<Boolean>,
    onSuggestionCountHandled: (Int) -> Unit
) {
    LaunchedEffect(
        suggestionCount
    ) {
        if (
            suggestionCount <= 0 ||
            messageCount <= 0
        ) {
            onSuggestionCountHandled(
                suggestionCount
            )

            return@LaunchedEffect
        }

        if (
            suggestionCount <=
            previousSuggestionCount
        ) {
            onSuggestionCountHandled(
                suggestionCount
            )

            return@LaunchedEffect
        }

        if (
            !shouldFollowBottom.value
        ) {
            onSuggestionCountHandled(
                suggestionCount
            )

            return@LaunchedEffect
        }

        delay(
            CHAT_SUGGESTION_SCROLL_DELAY_MILLIS
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
    bottomAnchorIndex: Int,
    initialBottomSettled: MutableState<Boolean>,
    shouldFollowBottom: MutableState<Boolean>
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

        if (
            !initialBottomSettled.value ||
            !shouldFollowBottom.value
        ) {
            return@LaunchedEffect
        }

        awaitChatLayoutFrames()

        delay(
            CHAT_OVERLAY_SCROLL_SETTLE_DELAY_MILLIS
        )

        if (
            !shouldFollowBottom.value
        ) {
            return@LaunchedEffect
        }

        animateToBottomSafely(
            listState =
                listState,

            bottomAnchorIndex =
                bottomAnchorIndex
        )

        delay(
            CHAT_OVERLAY_FINAL_CORRECTION_DELAY_MILLIS
        )

        awaitChatLayoutFrames()

        if (
            shouldFollowBottom.value
        ) {
            scrollToBottomSafely(
                listState =
                    listState,

                bottomAnchorIndex =
                    bottomAnchorIndex
            )
        }
    }
}

@Composable
private fun SendingScrollEffect(
    listState: LazyListState,
    messageCount: Int,
    isSending: Boolean,
    bottomAnchorIndex: Int,
    shouldFollowBottom: MutableState<Boolean>
) {
    LaunchedEffect(
        isSending
    ) {
        if (
            !isSending ||
            messageCount <= 0
        ) {
            return@LaunchedEffect
        }

        shouldFollowBottom.value =
            true

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
    val safeIndex =
        listState.safeBottomIndex(
            requestedIndex =
                bottomAnchorIndex
        )
            ?: return

    listState.animateScrollToItem(
        index =
            safeIndex,

        scrollOffset =
            0
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
        )
            ?: return

    listState.scrollToItem(
        index =
            safeIndex,

        scrollOffset =
            0
    )
}

private fun LazyListState.safeBottomIndex(
    requestedIndex: Int
): Int? {
    val totalItemCount =
        layoutInfo.totalItemsCount

    if (
        totalItemCount <= 0
    ) {
        return null
    }

    return requestedIndex.coerceIn(
        minimumValue =
            0,

        maximumValue =
            totalItemCount - 1
    )
}

private fun LazyListState.isAtConversationBottom():
        Boolean {

    val layout =
        layoutInfo

    val totalItemCount =
        layout.totalItemsCount

    if (
        totalItemCount <= 0
    ) {
        return true
    }

    /*
     * Compose already exposes the most reliable semantic answer
     * for this question.
     *
     * canScrollForward == false means there is no remaining
     * content below the current viewport.
     */
    if (
        !canScrollForward
    ) {
        return true
    }

    val lastVisibleItem =
        layout
            .visibleItemsInfo
            .lastOrNull()
            ?: return false

    val finalItemIndex =
        totalItemCount -
                1

    if (
        lastVisibleItem.index !=
        finalItemIndex
    ) {
        return false
    }

    val finalItemBottom =
        lastVisibleItem.offset +
                lastVisibleItem.size

    return finalItemBottom <=
            layout.viewportEndOffset +
            CHAT_BOTTOM_VISIBILITY_TOLERANCE_PX
}

private suspend fun awaitChatLayoutFrames() {
    withFrameNanos {
    }

    withFrameNanos {
    }
}

/*
 * These names intentionally use a Chat-specific prefix.
 *
 * The previous build failed because the project already
 * contained top-level KEYBOARD_SCROLL_DELAY_MILLIS and
 * CONTENT_SCROLL_DELAY_MILLIS declarations in this package.
 */
private const val CHAT_INITIAL_BOTTOM_SETTLE_DELAY_MILLIS =
    120L

private const val CHAT_KEYBOARD_SCROLL_DELAY_MILLIS =
    80L

private const val CHAT_CONTENT_SCROLL_DELAY_MILLIS =
    80L

private const val CHAT_MESSAGE_FINAL_CORRECTION_DELAY_MILLIS =
    120L

private const val CHAT_SUGGESTION_SCROLL_DELAY_MILLIS =
    160L

private const val CHAT_OVERLAY_SCROLL_SETTLE_DELAY_MILLIS =
    80L

private const val CHAT_OVERLAY_FINAL_CORRECTION_DELAY_MILLIS =
    120L

private const val CHAT_BOTTOM_VISIBILITY_TOLERANCE_PX =
    8