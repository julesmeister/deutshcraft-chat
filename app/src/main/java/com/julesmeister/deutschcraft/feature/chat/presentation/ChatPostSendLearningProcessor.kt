package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.feature.chat.data.local.ChatLocalDataSource
import com.julesmeister.deutschcraft.feature.chat.data.local.toDomain
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Resolves the exact persisted learner message and runs each
 * supplementary post-send learning system.
 *
 * Responsibilities:
 *
 * - Load the saved conversation
 * - Resolve the newest matching learner message
 * - Build history ending at that learner message
 * - Run mission evaluation
 * - Run contextual Besser
 * - Run adaptive learning
 *
 * Failures here must not convert a successful chat send into a
 * failed send.
 */
class ChatPostSendLearningProcessor
@Inject constructor(
    private val localDataSource:
    ChatLocalDataSource,

    private val besserProcessor:
    ChatBesserProcessor,

    private val missionProcessor:
    ChatMissionLearningProcessor,

    private val adaptiveLearningProcessor:
    ChatAdaptiveLearningProcessor
) {

    suspend fun process(
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
                            ChatPresentationTokens.PostSendLearningProcessor.ProcessorTag,
                            "Post-send learning was skipped " +
                                    "because the conversation was " +
                                    "not found. " +
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
                            ChatPresentationTokens.PostSendLearningProcessor.ProcessorTag,
                            "Post-send learning was skipped " +
                                    "because the newly saved learner " +
                                    "message could not be identified. " +
                                    "conversationId=$conversationId"
                        )

                        return ChatLearningProcessingResult()
                    }

            /*
             * Stop history at the learner message.
             *
             * The newly generated assistant reply already exists
             * in Room, but it must not appear before the learner
             * turn in any learning evaluation.
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
                    .toList()
                    .takeLast(
                        ChatPresentationTokens.PostSendLearningProcessor.RecentLearningMessageCount
                    )

            Log.d(
                ChatPresentationTokens.PostSendLearningProcessor.ProcessorTag,
                buildString {
                    append(
                        "Processing persisted learner message. "
                    )
                    append(
                        "messageId="
                    )
                    append(
                        savedLearnerMessage.id
                    )
                    append(
                        ", conversationId="
                    )
                    append(
                        conversationId
                    )
                    append(
                        ", recentMessages="
                    )
                    append(
                        recentMessages.size
                    )
                    append(
                        ", activeMission="
                    )
                    append(
                        activeMission?.id
                    )
                }
            )

            // Persist goal completion before supplementary work so Room can show it immediately.
            val missionResult =
                missionProcessor.process(
                    activeMission =
                        activeMission,
                    learnerMessage =
                        savedLearnerMessage,
                    recentMessages =
                        recentMessages,
                    cefrLevel =
                        conversation.cefrLevel
                )

            besserProcessor.process(
                conversationId =
                    conversationId,
                learnerMessage =
                    savedLearnerMessage,
                currentCefrLevel =
                    conversation.cefrLevel,
                activeMission =
                    activeMission
            )

            val adaptiveLearningResult =
                adaptiveLearningProcessor.process(
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
                missionEvaluationSummary =
                    missionResult
                        .evaluationSummary,

                adaptiveLearningResult =
                    adaptiveLearningResult,

                adaptiveLearningFeedback =
                    adaptiveLearningResult.feedback,

                completedMissionTitle =
                    missionResult
                        .completedMissionTitle,

                completedMissionAnalytics =
                    missionResult
                        .completedMissionAnalytics
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                ChatPresentationTokens.PostSendLearningProcessor.ProcessorTag,
                "Post-send learning processing failed.",
                throwable
            )

            ChatLearningProcessingResult()
        }
    }

    private fun findSavedLearnerMessage(
        messages: List<ChatMessage>,
        submittedMessageText: String
    ): ChatMessage? {
        val normalizedSubmittedText =
            submittedMessageText.trim()

        /*
         * Select the newest matching learner message.
         *
         * This avoids selecting an older duplicate when the
         * learner sends identical text multiple times.
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

}
