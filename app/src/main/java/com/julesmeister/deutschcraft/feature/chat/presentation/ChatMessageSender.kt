package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
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
    ChatMissionEvaluationCoordinator
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
         * - Running the language assessment
         * - Generating reply suggestions
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

        /*
         * Replace cached suggestions only after Emil's
         * response succeeds.
         */
        replySuggestionCache.replaceSuggestions(
            conversationId =
                conversationId,
            suggestions =
                suggestions
        )

        /*
         * Objective evaluation is supplementary and runs
         * only while this conversation has an active
         * mission.
         *
         * The evaluator receives recent conversation context
         * so it can recognize objectives completed before
         * the latest message.
         *
         * Example:
         *
         * If the learner greeted Emil several turns earlier,
         * the Begrüßung objective can still be completed
         * without forcing the learner to greet Emil again.
         */
        if (activeMission != null) {
            evaluateLearnerMessage(
                conversationId =
                    conversationId,
                submittedMessageText =
                    messageText,
                activeMission =
                    activeMission
            )
        } else {
            Log.d(
                SENDER_TAG,
                "Mission evaluation skipped because this " +
                        "conversation has no active mission."
            )
        }

        return ChatMessageSendResult(
            suggestions =
                suggestions,
            warning =
                result.assessmentWarning
        )
    }

    /*
     * Compatibility overload for callers that do not have an
     * active mission.
     *
     * Those callers perform the normal send flow but skip
     * mission-objective evaluation.
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

    private suspend fun evaluateLearnerMessage(
        conversationId: String,
        submittedMessageText: String,
        activeMission: ConversationMission
    ) {
        try {
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
                            "Mission evaluation skipped because " +
                                    "the conversation was not found. " +
                                    "Conversation ID=$conversationId"
                        )

                        return
                    }

            /*
             * Load the saved messages once.
             *
             * SendChatMessageUseCase has already saved:
             *
             * 1. The learner's message
             * 2. Emil's assistant response
             *
             * Therefore, the latest saved message may be
             * Emil's response rather than the learner's
             * message.
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
                            "Mission evaluation skipped because " +
                                    "the newly saved learner message " +
                                    "could not be identified. " +
                                    "Conversation ID=$conversationId"
                        )

                        return
                    }

            /*
             * Keep only the most recent conversation turns
             * to prevent the Qwen evaluator prompt from
             * growing indefinitely.
             *
             * The selected list contains both learner and
             * assistant messages in chronological order.
             * Assistant turns provide context, but only
             * learner turns can complete objectives.
             */
            val recentMessages =
                savedMessages
                    .filter { message ->
                        message.content.isNotBlank()
                    }
                    .takeLast(
                        RECENT_MISSION_MESSAGE_COUNT
                    )

            Log.d(
                SENDER_TAG,
                buildString {
                    append("Evaluating learner message ")
                    append(savedLearnerMessage.id)
                    append(" against mission ")
                    append(activeMission.id)
                    append(" using ")
                    append(recentMessages.size)
                    append(" recent conversation turns.")
                }
            )

            missionEvaluationCoordinator.evaluate(
                mission =
                    activeMission,
                learnerMessage =
                    savedLearnerMessage,
                recentMessages =
                    recentMessages,
                cefrLevel =
                    conversation.cefrLevel
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            /*
             * Mission evaluation must never turn an otherwise
             * successful chat response into a failed send.
             */
            Log.e(
                SENDER_TAG,
                "Mission evaluation could not be completed.",
                throwable
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
         * Select the newest USER message whose content
         * matches the submitted input.
         *
         * maxByOrNull protects against evaluating an older
         * duplicate message if the learner sent the same
         * text earlier in the conversation.
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

    fun clearSuggestions(
        conversationId: String
    ) {
        replySuggestionCache.clearSuggestions(
            conversationId =
                conversationId
        )
    }

    private companion object {
        const val SENDER_TAG =
            "ChatMessageSender"

        /*
         * Six learner/assistant exchanges provide enough
         * context to recognize recently completed objectives
         * without sending the entire conversation to Qwen.
         */
        const val RECENT_MISSION_MESSAGE_COUNT =
            12
    }
}

data class ChatMessageSendResult(
    val suggestions:
    List<ReplySuggestion>,
    val warning: String?
)