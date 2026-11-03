package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.besser.presentation.BesserChallengeBubble
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatUiState
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens as ChatTokens
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhrase
import com.julesmeister.deutschcraft.feature.phrasemarking.presentation.MarkedPhraseBubble
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatMessageList(
    uiState: ChatUiState,
    onMessageClick: (ChatMessage) -> Unit,
    modifier: Modifier = Modifier,
    scrollRequestKey: Int = 0,
    bottomOverlayHeight: Dp = 0.dp,

    markedPhrases: Map<String, List<MarkedPhrase>> =
        emptyMap(),

    onRemoveMarkedPhrase: (String) -> Unit =
        {},

    onBesserImprove: (String) -> Unit =
        {},

    onBesserHint: (String) -> Unit =
        {},

    onBesserSkip: (String) -> Unit =
        {},

    onMessageCopied: () -> Unit =
        {}
) {
    val messageCount =
        uiState.messages.size

    val hasTypingIndicator =
        uiState.isAssistantTyping

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

    val entries =
        remember(
            uiState.messages
        ) {
            buildChatListEntries(
                messages =
                    uiState.messages
            )
        }

    /*
     * The anchor is a real LazyColumn item placed after every
     * message and after the typing indicator.
     *
     * Date separators are included in entries, therefore
     * entries.size is the correct index base rather than
     * uiState.messages.size.
     */
    val bottomAnchorIndex =
        entries.size +
                if (hasTypingIndicator) {
                    1
                } else {
                    0
                }

    /*
     * Start directly on the bottom anchor.
     *
     * The follow-up scroll effect performs another correction
     * after the floating composer has reported its real height.
     * That second correction is important when reopening a
     * conversation because bottomOverlayHeight initially starts
     * at zero.
     */
    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex =
                bottomAnchorIndex
        )

    val density =
        LocalDensity.current

    val keyboardBottom =
        WindowInsets
            .ime
            .getBottom(
                density =
                    density
            )

    val latestUserMessageId =
        remember(
            uiState.messages
        ) {
            uiState
                .messages
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
            uiState
                .messages
                .lastOrNull { message ->
                    message.sender ==
                            MessageSender.ASSISTANT
                }
                ?.id
        }

    val latestVisibleBesserMessageId =
        remember(
            latestUserMessageId,
            uiState.besserChallenges
        ) {
            latestUserMessageId
                ?.takeIf { messageId ->
                    uiState
                        .besserChallenges
                        .containsKey(
                            messageId
                        )
                }
        }

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

    val sourceBubbleWidths =
        remember {
            mutableStateMapOf<String, Int>()
        }

    ChatMessageListScrollEffects(
        listState =
            listState,

        messageCount =
            messageCount,

        suggestionCount =
            uiState.suggestions.size,

        isSending =
            uiState.isSending,

        hasTypingIndicator =
            hasTypingIndicator,

        keyboardBottom =
            keyboardBottom,

        bottomOverlayHeight =
            bottomOverlayHeight,

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

        contentPadding =
            PaddingValues(
                top =
                    ChatTokens.MessageListTopPadding,

                bottom =
                    bottomOverlayHeight +
                            ChatTokens.MessageListBottomBreathingRoom
            )
    ) {
        items(
            items =
                entries,

            key = { entry ->
                entry.key
            },

            contentType = { entry ->
                when (entry) {
                    is ChatListEntry.DateSeparator ->
                        ChatPresentationTokens.MessageList.DateContentType

                    is ChatListEntry.Message ->
                        ChatPresentationTokens.MessageList.MessageContentType
                }
            }
        ) { entry ->
            when (entry) {
                is ChatListEntry.DateSeparator -> {
                    ChatDateSeparator(
                        label =
                            entry.label
                    )
                }

                is ChatListEntry.Message -> {
                    val message =
                        entry.message

                    val isUserMessage =
                        message.sender ==
                                MessageSender.USER

                    val attachedPhrases =
                        markedPhrases[
                            message.id
                        ].orEmpty()

                    val attachedChallenge =
                        uiState
                            .besserChallenges[
                            message.id
                        ]

                    val visibleChallenge =
                        attachedChallenge
                            ?.takeIf {
                                isUserMessage &&
                                        message.id ==
                                        latestVisibleBesserMessageId
                            }

                    val hasAttachedContent =
                        visibleChallenge != null ||
                                attachedPhrases.isNotEmpty()

                    Column(
                        modifier =
                            Modifier
                                .padding(
                                    start =
                                        ChatTokens.MessageHorizontalPadding,

                                    end =
                                        ChatTokens.MessageHorizontalPadding,

                                    top =
                                        if (
                                            entry.isFirstInGroup
                                        ) {
                                            ChatTokens
                                                .MessageGroupStartSpacing
                                        } else {
                                            ChatTokens
                                                .MessageGroupedSpacing
                                        }
                                )
                                .animateContentSize()
                    ) {
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

                            hasAttachedPhrase =
                                hasAttachedContent,

                            onAnimationStarted = {
                                seenMessageIds[
                                    message.id
                                ] =
                                    true
                            },

                            onClick = {
                                onMessageClick(
                                    message
                                )
                            },

                            onMessageCopied =
                                onMessageCopied,

                            onBubbleWidthChanged = { width ->
                                if (
                                    width > 0 &&
                                    sourceBubbleWidths[
                                        message.id
                                    ] !=
                                    width
                                ) {
                                    sourceBubbleWidths[
                                        message.id
                                    ] =
                                        width
                                }
                            },

                            modifier =
                                Modifier
                        )

                        visibleChallenge?.let { challenge ->
                            BesserChallengeBubble(
                                challenge =
                                    challenge,

                                isUserMessage =
                                    isUserMessage,

                                sourceBubbleWidthPx =
                                    sourceBubbleWidths[
                                        message.id
                                    ] ?: 0,

                                isLast =
                                    attachedPhrases.isEmpty(),

                                onImprove = {
                                    onBesserImprove(
                                        challenge.id
                                    )
                                },

                                onHint = {
                                    onBesserHint(
                                        challenge.id
                                    )
                                },

                                onSkip = {
                                    onBesserSkip(
                                        challenge.id
                                    )
                                }
                            )
                        }

                        attachedPhrases.forEachIndexed {
                                index,
                                phrase ->

                            MarkedPhraseBubble(
                                phrase =
                                    phrase,

                                isUserMessage =
                                    isUserMessage,

                                isLast =
                                    index ==
                                            attachedPhrases.lastIndex,

                                onRemove = {
                                    onRemoveMarkedPhrase(
                                        phrase.id
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        if (hasTypingIndicator) {
            item(
                key =
                    ChatTokens.TypingIndicatorKey
            ) {
                TypingIndicator(
                    modifier =
                        Modifier.padding(
                            start =
                                ChatTokens.MessageHorizontalPadding,

                            end =
                                ChatTokens.MessageHorizontalPadding,

                            top =
                                ChatTokens.MessageGroupStartSpacing
                        )
                )
            }
        }

        /*
         * Always keep one real item after the final visible
         * conversation item.
         *
         * Scrolling directly to this item is more reliable than
         * trying to calculate a pixel offset for an arbitrary
         * final message whose height may still be animating.
         */
        item(
            key =
                ChatTokens.BottomAnchorKey
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        ChatTokens.BottomAnchorHeight
                    )
            )
        }
    }
}

private sealed interface ChatListEntry {

    val key: String

    data class DateSeparator(
        val label: String,
        override val key: String
    ) : ChatListEntry

    data class Message(
        val message: ChatMessage,
        val isFirstInGroup: Boolean
    ) : ChatListEntry {

        override val key: String
            get() =
                message.id
    }
}

private fun buildChatListEntries(
    messages:
    List<ChatMessage>
): List<ChatListEntry> {

    val zone =
        ZoneId.systemDefault()

    val today =
        LocalDate.now(
            zone
        )

    val entries =
        mutableListOf<ChatListEntry>()

    var previousDate:
            LocalDate? =
        null

    var previousMessage:
            ChatMessage? =
        null

    messages.forEach { message ->
        val messageDate =
            Instant
                .ofEpochMilli(
                    message.timestamp
                )
                .atZone(
                    zone
                )
                .toLocalDate()

        val isNewDay =
            messageDate !=
                    previousDate

        if (isNewDay) {
            entries.add(
                ChatListEntry.DateSeparator(
                    label =
                        formatDateSeparator(
                            date =
                                messageDate,

                            today =
                                today
                        ),

                    key =
                        ChatPresentationTokens.MessageList.DateKeyPrefix +
                                messageDate
                )
            )
        }

        val previous =
            previousMessage

        val isFirstInGroup =
            isNewDay ||
                    previous == null ||
                    previous.sender !=
                    message.sender ||
                    message.timestamp -
                    previous.timestamp >
                    ChatTokens.MessageGroupTimeWindowMillis

        entries.add(
            ChatListEntry.Message(
                message =
                    message,

                isFirstInGroup =
                    isFirstInGroup
            )
        )

        previousDate =
            messageDate

        previousMessage =
            message
    }

    return entries
}

private fun formatDateSeparator(
    date:
    LocalDate,

    today:
    LocalDate
): String {

    return when {
        date == today -> {
            ChatPresentationTokens.MessageList.TodayLabel
        }

        date ==
                today.minusDays(
                    1
                ) -> {
            ChatPresentationTokens.MessageList.YesterdayLabel
        }

        date.year ==
                today.year -> {
            date.format(
                DateTimeFormatter.ofPattern(
                    ChatPresentationTokens.MessageList.CurrentYearDatePattern,
                    Locale.GERMAN
                )
            )
        }

        else -> {
            date.format(
                DateTimeFormatter.ofPattern(
                    ChatPresentationTokens.MessageList.OtherYearDatePattern,
                    Locale.GERMAN
                )
            )
        }
    }
}

@Composable
private fun ChatDateSeparator(
    label:
    String
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top =
                        ChatTokens.DateSeparatorTopPadding,

                    bottom =
                        ChatTokens.DateSeparatorBottomPadding
                ),

        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text =
                label,

            modifier =
                Modifier
                    .clip(
                        ChatTokens.DateSeparatorShape
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surfaceContainerHigh
                            .copy(
                                alpha =
                                    ChatTokens
                                        .DateSeparatorBackgroundAlpha
                            )
                    )
                    .padding(
                        horizontal =
                            ChatTokens.DateSeparatorHorizontalPadding,

                        vertical =
                            ChatTokens.DateSeparatorVerticalPadding
                    ),

            style =
                MaterialTheme
                    .typography
                    .labelSmall,

            fontWeight =
                FontWeight.Medium,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}

@Composable
private fun ChatMessageListEmptyState(
    modifier:
    Modifier
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
