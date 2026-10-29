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

    /*
     * =======================================================
     * MARKED PHRASES
     * =======================================================
     */

    markedPhrases: Map<String, List<MarkedPhrase>> =
        emptyMap(),

    onRemoveMarkedPhrase: (String) -> Unit =
        {},

    /*
     * =======================================================
     * BESSER
     * =======================================================
     */

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
     * Attachments remain inside their owning message item.
     *
     * Besser challenges and marked phrases do not add separate
     * LazyColumn item indices.
     */
    val bottomAnchorIndex =
        entries.size +
                if (hasTypingIndicator) {
                    1
                } else {
                    0
                }

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

    /*
     * =======================================================
     * LATEST MESSAGE IDS
     * =======================================================
     */

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

    /*
     * The active Besser bubble belongs only to the latest
     * learner message.
     *
     * If the latest learner message has no challenge yet, no
     * older challenge is displayed as a fallback.
     *
     * Older challenges remain persisted in Room but are hidden
     * from the conversation.
     */
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

    /*
     * =======================================================
     * MESSAGE ANIMATION STATE
     * =======================================================
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

    /*
     * =======================================================
     * SOURCE BUBBLE WIDTHS
     * =======================================================
     *
     * Stores:
     *
     * messageId -> actual rendered source-bubble width in px
     *
     * BesserChallengeBubble uses this measurement to determine
     * whether its exposed upper corner should be rounded.
     */
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
                        DATE_CONTENT_TYPE

                    is ChatListEntry.Message ->
                        MESSAGE_CONTENT_TYPE
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

                    /*
                     * ===========================================
                     * MARKED PHRASES
                     * ===========================================
                     */

                    val attachedPhrases =
                        markedPhrases[
                            message.id
                        ].orEmpty()

                    /*
                     * ===========================================
                     * BESSER
                     * ===========================================
                     */

                    val attachedChallenge =
                        uiState
                            .besserChallenges[
                            message.id
                        ]

                    /*
                     * A Besser challenge is rendered only if:
                     *
                     * 1. It belongs to a learner message.
                     * 2. That learner message is the latest
                     *    learner message.
                     * 3. The latest learner message currently has
                     *    an active challenge in ChatUiState.
                     *
                     * This intentionally does not fall back to an
                     * older challenge while a newer challenge is
                     * still being generated.
                     */
                    val visibleChallenge =
                        attachedChallenge
                            ?.takeIf {
                                isUserMessage &&
                                        message.id ==
                                        latestVisibleBesserMessageId
                            }

                    /*
                     * Flatten the message's connecting corner only
                     * when something is actually visible beneath
                     * that message.
                     *
                     * Hidden older challenges must not alter the
                     * source bubble's shape.
                     */
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
                        /*
                         * =======================================
                         * SOURCE MESSAGE
                         * =======================================
                         */

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

                            /*
                             * This historical parameter now means
                             * any visible attachment under the
                             * source message.
                             */
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

                            /*
                             * Measure only the actual MessageBubble
                             * container, not the full-width row.
                             */
                            onBubbleWidthChanged = { width ->
                                if (
                                    width > 0 &&
                                    sourceBubbleWidths[
                                        message.id
                                    ] != width
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

                        /*
                         * =======================================
                         * LATEST BESSER CHALLENGE
                         * =======================================
                         */

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

                                /*
                                 * Besser owns rounded bottom
                                 * corners only when no marked
                                 * phrases continue beneath it.
                                 */
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

                        /*
                         * =======================================
                         * MARKED PHRASES
                         * =======================================
                         */

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

        /*
         * ===================================================
         * TYPING INDICATOR
         * ===================================================
         */

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
         * ===================================================
         * BOTTOM ANCHOR
         * ===================================================
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

/*
 * ===========================================================
 * LIST ENTRIES
 * ===========================================================
 */

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
    messages: List<ChatMessage>
): List<ChatListEntry> {
    val zone =
        ZoneId.systemDefault()

    val today =
        LocalDate.now(
            zone
        )

    val entries =
        mutableListOf<ChatListEntry>()

    var previousDate: LocalDate? =
        null

    var previousMessage: ChatMessage? =
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
                        DATE_KEY_PREFIX +
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

/*
 * ===========================================================
 * DATE FORMATTING
 * ===========================================================
 */

private fun formatDateSeparator(
    date: LocalDate,
    today: LocalDate
): String {
    return when {
        date == today -> {
            TODAY_LABEL
        }

        date ==
                today.minusDays(
                    1
                ) -> {
            YESTERDAY_LABEL
        }

        date.year ==
                today.year -> {
            date.format(
                DateTimeFormatter.ofPattern(
                    CURRENT_YEAR_DATE_PATTERN,
                    Locale.GERMAN
                )
            )
        }

        else -> {
            date.format(
                DateTimeFormatter.ofPattern(
                    OTHER_YEAR_DATE_PATTERN,
                    Locale.GERMAN
                )
            )
        }
    }
}

/*
 * ===========================================================
 * DATE SEPARATOR
 * ===========================================================
 */

@Composable
private fun ChatDateSeparator(
    label: String
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

/*
 * ===========================================================
 * EMPTY STATE
 * ===========================================================
 */

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

/*
 * ===========================================================
 * CONSTANTS
 * ===========================================================
 */

private const val DATE_CONTENT_TYPE =
    "date"

private const val MESSAGE_CONTENT_TYPE =
    "message"

private const val DATE_KEY_PREFIX =
    "date-"

private const val TODAY_LABEL =
    "Heute"

private const val YESTERDAY_LABEL =
    "Gestern"

private const val CURRENT_YEAR_DATE_PATTERN =
    "EEEE, d. MMMM"

private const val OTHER_YEAR_DATE_PATTERN =
    "d. MMMM yyyy"