package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFeedback
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningMessageResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.usecase.RunAdaptiveLearningForMessageUseCase
import com.julesmeister.deutschcraft.feature.chat.data.cache.ReplySuggestionCache
import com.julesmeister.deutschcraft.feature.chat.data.local.ChatLocalDataSource
import com.julesmeister.deutschcraft.feature.chat.data.local.toDomain
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.SendChatMessageUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class ChatMessageSender @Inject constructor(
    private val sendChatMessage:
    SendChatMessageUseCase,
    private val localDataSource:
    ChatLocalDataSource,
    private val replySuggestionCache:
    ReplySuggestionCache,
    private val missionEvaluationCoordinator:
    ChatMissionEvaluationCoordinator,
    private val runAdaptiveLearningForMessage:
    RunAdaptiveLearningForMessageUseCase
) {

    suspend fun send(
        conversationId: String,
        messageText: String,
        activeMission:
        ConversationMission?
    ): ChatMessageSendResult {
        /*
         * The active mission must enter the send use case
         * before assistant generation begins.
         *
         * This is the critical connection that allows Emil
         * to adopt the mission role and focus on the current
         * objective.
         */
        val result =
            sendChatMessage(
                conversationId =
                    conversationId,
                messageText =
                    messageText,
                activeMission =
                    activeMission
            )

        val suggestions =
            result
                .assistantReply
                .suggestions

        replySuggestionCache.replaceSuggestions(
            conversationId =
                conversationId,
            suggestions =
                suggestions
        )

        val learningResult =
            processSavedLearnerMessage(
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
                result.assessmentWarning,
            adaptiveLearningFeedback =
                learningResult
                    .adaptiveLearningFeedback
        )
    }

    /*
     * Compatibility overload for callers that do not yet
     * supply active mission state.
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

    /*
     * Loads the saved learner message and recent conversation
     * once, then shares them between mission evaluation and
     * adaptive learning.
     *
     * At this point, SendChatMessageUseCase has already saved:
     *
     * - The learner message
     * - The assistant response
     * - Any assessment evidence
     * - Any correction
     */
    private suspend fun processSavedLearnerMessage(
        conversationId: String,
        submittedMessageText: String,
        activeMission:
        ConversationMission?
    ): ChatLearningProcessingResult {
        return try {
            val conversation =
                localDataSource
                    .getConversation(
                        conversationId =
                            conversationId
                    )
                    ?.toDomain()
                    ?: run {
                        Log.w(
                            SENDER_TAG,
                            "Post-send learning processing " +
                                    "was skipped because the " +
                                    "conversation was not found. " +
                                    "conversationId=$conversationId"
                        )

                        return ChatLearningProcessingResult()
                    }

            val savedMessages =
                localDataSource
                    .getMessages(
                        conversationId =
                            conversationId
                    )
                    .map { entity ->
                        entity.toDomain()
                    }
                    .sortedBy { message ->
                        message.sequenceNumber
                    }

            val savedLearnerMessage =
                findSavedLearnerMessage(
                    messages =
                        savedMessages,
                    submittedMessageText =
                        submittedMessageText
                )
                    ?: run {
                        Log.w(
                            SENDER_TAG,
                            "Post-send learning processing " +
                                    "was skipped because the newly " +
                                    "saved learner message could " +
                                    "not be identified. " +
                                    "conversationId=$conversationId"
                        )

                        return ChatLearningProcessingResult()
                    }

            /*
             * Stop at the current learner message.
             *
             * The assistant response has already been saved,
             * but it must not be included as if it occurred
             * before the learner message under evaluation.
             */
            val recentMessages =
                savedMessages
                    .asSequence()
                    .filter { message ->
                        message.content.isNotBlank()
                    }
                    .filter { message ->
                        message.sequenceNumber <=
                                savedLearnerMessage.sequenceNumber
                    }
                    .takeLastAsList(
                        maximumCount =
                            RECENT_LEARNING_MESSAGE_COUNT
                    )

            Log.d(
                SENDER_TAG,
                buildString {
                    append(
                        "Processing saved learner message. "
                    )
                    append("messageId=")
                    append(savedLearnerMessage.id)
                    append(", conversationId=")
                    append(conversationId)
                    append(", recentMessages=")
                    append(recentMessages.size)
                    append(", activeMission=")
                    append(activeMission?.id)
                }
            )

            evaluateMissionIfActive(
                activeMission =
                    activeMission,
                learnerMessage =
                    savedLearnerMessage,
                recentMessages =
                    recentMessages,
                cefrLevel =
                    conversation.cefrLevel
            )

            val adaptiveLearningResult =
                runAdaptiveLearning(
                    learnerMessage =
                        savedLearnerMessage,
                    conversationId =
                        conversationId,
                    recentMessages =
                        recentMessages,
                    cefrLevel =
                        conversation.cefrLevel
                )

            ChatLearningProcessingResult(
                adaptiveLearningResult =
                    adaptiveLearningResult,
                adaptiveLearningFeedback =
                    adaptiveLearningResult.feedback
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            /*
             * The normal chat operation has already
             * succeeded.
             *
             * Mission or adaptive-learning failures must not
             * convert the successful send into an error.
             */
            Log.e(
                SENDER_TAG,
                "Post-send learning processing failed.",
                throwable
            )

            ChatLearningProcessingResult()
        }
    }

    private suspend fun evaluateMissionIfActive(
        activeMission:
        ConversationMission?,
        learnerMessage: ChatMessage,
        recentMessages:
        List<ChatMessage>,
        cefrLevel: CefrLevel
    ) {
        if (activeMission == null) {
            Log.d(
                SENDER_TAG,
                "Mission evaluation skipped because this " +
                        "conversation has no active mission."
            )

            return
        }

        try {
            missionEvaluationCoordinator.evaluate(
                mission =
                    activeMission,
                learnerMessage =
                    learnerMessage,
                recentMessages =
                    recentMessages,
                cefrLevel =
                    cefrLevel
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                SENDER_TAG,
                "Mission evaluation could not be completed.",
                throwable
            )
        }
    }

    private suspend fun runAdaptiveLearning(
        learnerMessage: ChatMessage,
        conversationId: String,
        recentMessages:
        List<ChatMessage>,
        cefrLevel: CefrLevel
    ): AdaptiveLearningMessageResult {
        return try {
            val result =
                runAdaptiveLearningForMessage(
                    learnerMessage =
                        learnerMessage,
                    conversationId =
                        conversationId,
                    cefrLevel =
                        cefrLevel,
                    recentMessages =
                        recentMessages
                )

            logAdaptiveLearningResult(
                learnerMessage =
                    learnerMessage,
                result =
                    result
            )

            result
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                SENDER_TAG,
                "Adaptive learning could not be completed.",
                throwable
            )

            AdaptiveLearningMessageResult(
                failedObservationCount =
                    1
            )
        }
    }

    private fun findSavedLearnerMessage(
        messages: List<ChatMessage>,
        submittedMessageText: String
    ): ChatMessage? {
        val normalizedSubmittedText =
            submittedMessageText.trim()

        /*
         * Select the newest matching USER message.
         *
         * This avoids selecting an older duplicate when the
         * learner sends identical text more than once.
         */
        return messages
            .asSequence()
            .filter { message ->
                message.sender ==
                        MessageSender.USER
            }
            .filter { message ->
                message.content.trim() ==
                        normalizedSubmittedText
            }
            .maxByOrNull { message ->
                message.sequenceNumber
            }
    }

    private fun logAdaptiveLearningResult(
        learnerMessage: ChatMessage,
        result:
        AdaptiveLearningMessageResult
    ) {
        Log.d(
            ADAPTIVE_LEARNING_TAG,
            buildString {
                append(
                    "Adaptive processing finished. "
                )
                append("messageId=")
                append(learnerMessage.id)
                append(", recorded=")
                append(result.recordedFocusCount)
                append(", skipped=")
                append(result.skippedObservationCount)
                append(", failed=")
                append(result.failedObservationCount)
                append(", feedback=")
                append(result.feedback?.type)
            }
        )

        result.feedback?.let { feedback ->
            Log.d(
                ADAPTIVE_FEEDBACK_TAG,
                buildString {
                    append("type=")
                    append(feedback.type)
                    append(", focus=")
                    append(feedback.focusKey)
                    append(", message=")
                    append(feedback.message)
                }
            )
        }
    }

    fun clearSuggestions(
        conversationId: String
    ) {
        replySuggestionCache.clearSuggestions(
            conversationId =
                conversationId
        )
    }

    private fun <T> Sequence<T>.takeLastAsList(
        maximumCount: Int
    ): List<T> {
        if (maximumCount <= 0) {
            return emptyList()
        }

        return toList()
            .takeLast(
                maximumCount
            )
    }

    private companion object {
        const val SENDER_TAG =
            "ChatMessageSender"

        const val ADAPTIVE_LEARNING_TAG =
            "AdaptiveLearningChat"

        const val ADAPTIVE_FEEDBACK_TAG =
            "AdaptiveFeedback"

        const val RECENT_LEARNING_MESSAGE_COUNT =
            12
    }
}

/*
 * Internal result used while ChatMessageSender coordinates
 * mission evaluation and adaptive learning.
 */
private data class ChatLearningProcessingResult(
    val adaptiveLearningResult:
    AdaptiveLearningMessageResult =
        AdaptiveLearningMessageResult(),

    val adaptiveLearningFeedback:
    AdaptiveLearningFeedback? =
        null
)

/*
 * Public result returned to the chat presentation layer.
 */
data class ChatMessageSendResult(
    val suggestions:
    List<ReplySuggestion>,

    val warning: String?,

    val adaptiveLearningFeedback:
    AdaptiveLearningFeedback? =
        null
)