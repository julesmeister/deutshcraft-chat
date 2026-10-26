package com.julesmeister.deutschcraft.feature.adaptivelearning.data.mapper

import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveConversationSpeaker
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveConversationTurn
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import javax.inject.Inject

/*
 * Converts stored chat messages into the lightweight
 * conversation context used by adaptive-learning AI.
 *
 * The adaptive evaluator does not need the entire
 * ChatMessage domain model. It only needs:
 *
 * - Who produced the message
 * - The message text
 */
class ChatMessageAdaptiveConversationMapper
@Inject constructor() {

    fun map(
        message: ChatMessage
    ): AdaptiveConversationTurn? {
        val content =
            message.content.trim()

        if (content.isBlank()) {
            return null
        }

        val speaker =
            when (message.sender) {
                MessageSender.USER -> {
                    AdaptiveConversationSpeaker.LEARNER
                }

                else -> {
                    AdaptiveConversationSpeaker.ASSISTANT
                }
            }

        return AdaptiveConversationTurn(
            speaker =
                speaker,
            content =
                content.take(
                    MAXIMUM_TURN_LENGTH
                )
        )
    }

    fun mapRecent(
        messages: List<ChatMessage>,
        latestLearnerMessageId: String? =
            null
    ): List<AdaptiveConversationTurn> {
        val orderedMessages =
            messages
                .asSequence()
                .filter { message ->
                    message.content.isNotBlank()
                }
                .sortedBy { message ->
                    message.sequenceNumber
                }
                .toList()

        /*
         * When a latest learner message ID is available, stop
         * the context at that message.
         *
         * This prevents Emil's response, which may already
         * have been saved, from appearing after the learner
         * message being evaluated.
         */
        val boundedMessages =
            if (
                latestLearnerMessageId
                    .isNullOrBlank()
            ) {
                orderedMessages
            } else {
                val learnerMessageIndex =
                    orderedMessages.indexOfLast { message ->
                        message.id ==
                                latestLearnerMessageId
                    }

                if (learnerMessageIndex >= 0) {
                    orderedMessages.take(
                        learnerMessageIndex + 1
                    )
                } else {
                    orderedMessages
                }
            }

        return boundedMessages
            .takeLast(
                MAXIMUM_RECENT_TURN_COUNT
            )
            .mapNotNull(
                ::map
            )
    }

    private companion object {
        const val MAXIMUM_RECENT_TURN_COUNT =
            10

        const val MAXIMUM_TURN_LENGTH =
            500
    }
}