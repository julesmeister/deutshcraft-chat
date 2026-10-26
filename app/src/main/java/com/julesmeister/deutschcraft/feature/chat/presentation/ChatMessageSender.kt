package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
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
        activeMission: ConversationMission?
    ): ChatMessageSendResult {
        /*
         * Complete the normal chat operation first.
         *
         * SendChatMessageUseCase remains responsible for:
         *
         * - Saving the learner message
         * - Generating Emil's reply
         * - Saving Emil's reply
         * - Running the existing language assessment
         * - Saving detected mistakes
         * - Generating reply suggestions
         *
         * Adaptive learning runs only after this operation
         * succeeds, because it consumes the saved assessment
         * and detected-mistake records.
         */
        val result =
            sendChatMessage(
                conversationId =
                    conversationId,
                messageText =
                    messageText
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
                learningResult.adaptiveLearningFeedback
        )
    }

    /*
     * Compatibility overload for callers that do not yet
     * provide active mission state.
     *
     * Adaptive learning still runs because it does not
     * require an active mission.
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
     * At this point SendChatMessageUseCase has already saved
     * both the learner message and Emil's reply.
     */
    private suspend fun processSavedLearnerMessage(
        conversationId: String,
        submittedMessageText: String,
        activeMission: ConversationMission?
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
                                    "skipped because the conversation " +
                                    "was not found. " +
                                    "conversationId=$conversationId"
                        )

                        return ChatLearningProcessingResult()
                    }

            /*
             * Load and map messages once.
             *
             * The final saved message may belong to Emil, so
             * it must not automatically be treated as the
             * learner message being evaluated.
             */
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
                                    "skipped because the newly saved " +
                                    "learner message could not be " +
                                    "identified. " +
                                    "conversationId=$conversationId"
                        )

                        return ChatLearningProcessingResult()
                    }

            /*
             * Include both learner and assistant turns.
             *
             * Mission evaluation can inspect previous learner
             * turns, while adaptive communication evaluation
             * uses Emil's turns to understand what behavior
             * the learner was invited to demonstrate.
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
                    append("Processing saved learner message. ")
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
             * The chat operation already succeeded.
             *
             * Mission or adaptive-learning failures must not
             * transform the successful send into an error.
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
        activeMission: ConversationMission?,
        learnerMessage: ChatMessage,
        recentMessages: List<ChatMessage>,
        cefrLevel:
        com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
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
        recentMessages: List<ChatMessage>,
        cefrLevel:
        com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
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
         * Choose the newest USER message matching the text
         * that was submitted.
         *
         * maxByOrNull protects against selecting an older
         * duplicate when the learner has sent the same text
         * previously.
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
        result: AdaptiveLearningMessageResult
    ) {
        Log.d(
            ADAPTIVE_LEARNING_TAG,
            buildString {
                append("Adaptive processing finished. ")
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

        /*
         * Provides up to six learner-assistant exchanges.
         *
         * This is sufficient for understanding the current
         * communication opportunity without sending the
         * entire thread to Qwen.
         */
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
 *
 * adaptiveLearningFeedback has a default value so existing
 * call sites that construct this result with suggestions and
 * warning continue to compile.
 */
data class ChatMessageSendResult(
    val suggestions:
    List<ReplySuggestion>,

    val warning: String?,

    val adaptiveLearningFeedback:
    AdaptiveLearningFeedback? =
        null
)