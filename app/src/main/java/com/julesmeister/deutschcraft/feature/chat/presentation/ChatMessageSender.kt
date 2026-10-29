package com.julesmeister.deutschcraft.feature.chat.presentation

import com.julesmeister.deutschcraft.feature.chat.data.cache.ReplySuggestionCache
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.SendChatMessageUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import javax.inject.Inject

/*
 * Coordinates the primary chat send.
 *
 * Responsibilities:
 *
 * - Generate and persist the learner/assistant turn
 * - Cache reply suggestions
 * - Delegate post-send learning processing
 * - Return the final presentation result
 */
class ChatMessageSender
@Inject constructor(
    private val sendChatMessage:
    SendChatMessageUseCase,

    private val replySuggestionCache:
    ReplySuggestionCache,

    private val postSendLearningProcessor:
    ChatPostSendLearningProcessor
) {

    suspend fun send(
        conversationId: String,
        messageText: String,
        activeMission: ConversationMission?
    ): ChatMessageSendResult {
        val sendResult =
            sendChatMessage(
                conversationId =
                    conversationId,
                messageText =
                    messageText,
                activeMission =
                    activeMission
            )

        val suggestions =
            sendResult
                .assistantReply
                .suggestions

        replySuggestionCache.replaceSuggestions(
            conversationId =
                conversationId,
            suggestions =
                suggestions
        )

        /*
         * The learner and assistant messages now exist in Room.
         *
         * Supplementary learning systems can safely resolve the
         * exact persisted learner message.
         */
        val learningResult =
            postSendLearningProcessor.process(
                conversationId =
                    conversationId,
                submittedMessageText =
                    messageText,
                activeMission =
                    activeMission
            )

        return ChatMessageSendResult(
            suggestions =
                suggestions,

            warning =
                sendResult.assessmentWarning,

            adaptiveLearningFeedback =
                learningResult
                    .adaptiveLearningFeedback,

            completedMissionTitle =
                learningResult
                    .completedMissionTitle,

            completedMissionAnalytics =
                learningResult
                    .completedMissionAnalytics
        )
    }

    /*
     * Compatibility overload for callers that do not provide
     * active mission state.
     */
    suspend fun send(
        conversationId: String,
        messageText: String
    ): ChatMessageSendResult {
        return send(
            conversationId =
                conversationId,
            messageText =
                messageText,
            activeMission =
                null
        )
    }

    fun clearSuggestions(
        conversationId: String
    ) {
        replySuggestionCache.clearSuggestions(
            conversationId =
                conversationId
        )
    }
}