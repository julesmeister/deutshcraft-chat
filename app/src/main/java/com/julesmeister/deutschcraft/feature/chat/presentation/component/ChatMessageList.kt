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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.julesmeister.deutschcraft.feature.besser.presentation.BesserChallengeUiModel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatUiState
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
     *
     * messageId -> learner-marked phrases attached to the
     * corresponding conversation message.
     */
    markedPhrases: Map<String, List<MarkedPhrase>> =
        emptyMap(),

    onRemoveMarkedPhrase: (String) -> Unit =
        {},

    /*
     * =======================================================
     * BESSER CHALLENGES
     * =======================================================
     *
     * messageId -> contextual growth challenge associated
     * with that learner message.
     *
     * There can currently be at most one active challenge per
     * message.
     */
    besserChallenges: Map<String, BesserChallengeUiModel> =
        emptyMap(),

    /*
     * challengeId
     *
     * Starts message refinement mode.
     */
    onBesserImprove: (String) -> Unit =
        {},

    /*
     * challengeId
     *
     * Reveals the next available assistance step.
     */
    onBesserHint: (String) -> Unit =
        {},

    /*
     * challengeId
     *
     * Dismisses the challenge without penalizing the learner.
     */
    onBesserSkip: (String) -> Unit =
        {},

    /*
     * Fired after the visible primary message text has been
     * successfully copied through MessageBubble's long-press
     * action.
     */
    onMessageCopied: () -> Unit =
        {}
) {
    val messageCount =
        uiState.messages.size

    val hasTypingIndicator =
        uiState.isAssistantTyping

    /*
     * Room commonly emits an empty conversation before the
     * actual messages arrive.
     *
     * Avoid creating a LazyListState for an entirely empty
     * conversation.
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
     * Attachments live INSIDE their owning message item.
     *
     * Therefore:
     *
     * - MarkedPhraseBubble
     * - BesserChallengeBubble
     *
     * do not increase the LazyColumn item count.
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
     * Existing messages must not replay their entrance
     * animations whenever the conversation screen opens.
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
                    MESSAGE_LIST_TOP_PADDING,
                bottom =
                    bottomOverlayHeight +
                            MESSAGE_LIST_BOTTOM_BREATHING_ROOM
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
                        "date"

                    is ChatListEntry.Message ->
                        "message"
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
                     * Existing phrase-marking attachments.
                     */
                    val attachedPhrases =
                        markedPhrases[
                            message.id
                        ].orEmpty()

                    /*
                     * New contextual Besser Challenge.
                     *
                     * The challenge belongs to this exact
                     * message through message.id.
                     */
                    val attachedChallenge =
                        besserChallenges[
                            message.id
                        ]

                    /*
                     * Besser growth challenges should only be
                     * displayed underneath learner messages.
                     *
                     * Even if malformed state accidentally
                     * associates one with an assistant message,
                     * the UI will refuse to render it there.
                     */
                    val visibleChallenge =
                        attachedChallenge
                            ?.takeIf {
                                isUserMessage
                            }

                    /*
                     * MessageBubble currently receives a
                     * hasAttachedPhrase flag to flatten the
                     * attachment-side corner.
                     *
                     * A Besser Challenge needs the exact same
                     * geometric attachment behavior.
                     *
                     * We therefore interpret this parameter as
                     * "has attached content" for now without
                     * requiring an immediate MessageBubble API
                     * rename.
                     */
                    val hasAttachedContent =
                        visibleChallenge !=
                                null ||
                                attachedPhrases
                                    .isNotEmpty()

                    Column(
                        modifier =
                            Modifier
                                .padding(
                                    start =
                                        MESSAGE_HORIZONTAL_PADDING,
                                    end =
                                        MESSAGE_HORIZONTAL_PADDING,
                                    top =
                                        if (
                                            entry.isFirstInGroup
                                        ) {
                                            GROUP_START_SPACING
                                        } else {
                                            GROUPED_MESSAGE_SPACING
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

                            /*
                             * Despite the historical parameter
                             * name, this now means:
                             *
                             * Does this message have something
                             * visually attached beneath it?
                             *
                             * That includes:
                             *
                             * - Besser Challenges
                             * - Marked Phrase notes
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

                            modifier =
                                Modifier
                        )

                        /*
                         * ===================================================
                         * BESSER CHALLENGE
                         * ===================================================
                         *
                         * Challenges stay visually closest to the source
                         * learner message because the challenge asks the
                         * learner to refine that exact communicative turn.
                         *
                         * Order:
                         *
                         * User message
                         *     ↓
                         * Besser Challenge
                         *     ↓
                         * Marked Phrase notes
                         */
                        if (
                            visibleChallenge !=
                            null
                        ) {
                            BesserChallengeBubble(
                                challenge =
                                    visibleChallenge,
                                isUserMessage =
                                    true,
                                onImprove = {
                                    onBesserImprove(
                                        visibleChallenge.id
                                    )
                                },
                                onHint = {
                                    onBesserHint(
                                        visibleChallenge.id
                                    )
                                },
                                onSkip = {
                                    onBesserSkip(
                                        visibleChallenge.id
                                    )
                                }
                            )
                        }

                        /*
                         * ===================================================
                         * MARKED PHRASES
                         * ===================================================
                         *
                         * Phrase notes remain attached to their original
                         * message.
                         */
                        attachedPhrases.forEachIndexed { index, phrase ->
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
                    TYPING_INDICATOR_KEY
            ) {
                TypingIndicator(
                    modifier =
                        Modifier.padding(
                            start =
                                MESSAGE_HORIZONTAL_PADDING,
                            end =
                                MESSAGE_HORIZONTAL_PADDING,
                            top =
                                GROUP_START_SPACING
                        )
                )
            }
        }

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
                        "date-$messageDate"
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
                    GROUP_TIME_WINDOW_MILLIS

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
            "Heute"
        }

        date == today.minusDays(1) -> {
            "Gestern"
        }

        date.year ==
                today.year -> {
            date.format(
                DateTimeFormatter.ofPattern(
                    "EEEE, d. MMMM",
                    Locale.GERMAN
                )
            )
        }

        else -> {
            date.format(
                DateTimeFormatter.ofPattern(
                    "d. MMMM yyyy",
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
                        DATE_SEPARATOR_TOP_PADDING,
                    bottom =
                        DATE_SEPARATOR_BOTTOM_PADDING
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
                        RoundedCornerShape(
                            percent =
                                FULL_CORNER_PERCENT
                        )
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surfaceContainerHigh
                            .copy(
                                alpha =
                                    DATE_SEPARATOR_BACKGROUND_ALPHA
                            )
                    )
                    .padding(
                        horizontal =
                            DATE_SEPARATOR_HORIZONTAL_PADDING,
                        vertical =
                            DATE_SEPARATOR_VERTICAL_PADDING
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
 * LIST BEHAVIOR
 * ===========================================================
 */

private const val GROUP_TIME_WINDOW_MILLIS =
    5 * 60 * 1_000L

private const val TYPING_INDICATOR_KEY =
    "typing-indicator"

private const val BOTTOM_ANCHOR_KEY =
    "bottom-anchor"

/*
 * ===========================================================
 * DIMENSIONS
 * ===========================================================
 */

private val MESSAGE_LIST_TOP_PADDING =
    4.dp

private val MESSAGE_LIST_BOTTOM_BREATHING_ROOM =
    8.dp

private val MESSAGE_HORIZONTAL_PADDING =
    12.dp

private val GROUP_START_SPACING =
    12.dp

private val GROUPED_MESSAGE_SPACING =
    2.dp

private val DATE_SEPARATOR_TOP_PADDING =
    16.dp

private val DATE_SEPARATOR_BOTTOM_PADDING =
    4.dp

private val DATE_SEPARATOR_HORIZONTAL_PADDING =
    12.dp

private val DATE_SEPARATOR_VERTICAL_PADDING =
    4.dp

private const val FULL_CORNER_PERCENT =
    50

private const val DATE_SEPARATOR_BACKGROUND_ALPHA =
    0.85f