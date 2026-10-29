package com.julesmeister.deutschcraft.feature.chat.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.activity.ActivityTracker
import com.julesmeister.deutschcraft.core.cefr.model.DefaultLearnerProfile
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.engagement.domain.ScheduleIdlePromptUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SendChatMessageUseCase @Inject constructor(
    private val chatRepository:
    ChatRepository,

    private val activityTracker:
    ActivityTracker,

    private val assessChatMessage:
    AssessChatMessageUseCase,

    private val correctLearnerMessage:
    CorrectLearnerMessageUseCase,

    private val scheduleIdlePrompt:
    ScheduleIdlePromptUseCase,

    private val clock:
    Clock
) {

    suspend operator fun invoke(
        conversationId: String,
        messageText: String,
        activeMission:
        ConversationMission? =
            null
    ): SendChatMessageResult =
        coroutineScope {
            val cleanMessage =
                messageText.trim()

            require(
                conversationId.isNotBlank()
            ) {
                "Conversation ID cannot be blank."
            }

            require(
                cleanMessage.isNotBlank()
            ) {
                "The message must not be blank."
            }

            /*
             * =================================================
             * COMMIT PROVISIONAL CONVERSATION
             * =================================================
             *
             * Opening New Conversation no longer creates a Room
             * conversation.
             *
             * The first real learner send is the commit point.
             */
            ensureConversationExists(
                conversationId =
                    conversationId
            )

            /*
             * ConversationFactory inserts its opening assistant
             * greeting as sequence 1.
             *
             * Therefore the first learner message receives
             * sequence 2.
             */
            val userMessage =
                createUserMessage(
                    conversationId =
                        conversationId,
                    content =
                        cleanMessage
                )

            /*
             * Save the learner message.
             */
            chatRepository.saveMessage(
                message =
                    userMessage
            )

            scheduleIdlePrompt()

            val correctionDeferred =
                async {
                    correctAndPersist(
                        userMessage =
                            userMessage
                    )
                }

            val assistantReplyDeferred =
                async {
                    chatRepository
                        .generateAssistantReply(
                            conversationId =
                                conversationId,
                            message =
                                cleanMessage,
                            activeMission =
                                activeMission
                        )
                }

            val activeDayCount =
                activityTracker
                    .recordMessageActivity(
                        profileId =
                            DefaultLearnerProfile
                                .LOCAL_PROFILE_ID
                    )

            val assessmentWarning =
                assessWithoutBreakingChat(
                    userMessage =
                        userMessage,
                    activeDayCount =
                        activeDayCount
                )

            correctionDeferred.await()

            val assistantReply =
                assistantReplyDeferred.await()

            chatRepository.saveMessage(
                message =
                    assistantReply.message
            )

            SendChatMessageResult(
                assistantReply =
                    assistantReply,
                assessmentWarning =
                    assessmentWarning
            )
        }

    /*
     * A provisional ordinary conversation becomes persistent
     * only when its first learner message is sent.
     *
     * Existing conversations pass through without mutation.
     */
    private suspend fun ensureConversationExists(
        conversationId: String
    ) {
        val existingConversation =
            chatRepository.getConversation(
                conversationId =
                    conversationId
            )

        if (existingConversation != null) {
            return
        }

        chatRepository.createConversation(
            conversationId =
                conversationId
        )
    }

    private suspend fun createUserMessage(
        conversationId: String,
        content: String
    ): ChatMessage {
        val sequenceNumber =
            chatRepository
                .getNextSequenceNumber(
                    conversationId =
                        conversationId
                )

        return ChatMessage(
            id =
                UUID.randomUUID().toString(),
            conversationId =
                conversationId,
            content =
                content,
            sender =
                MessageSender.USER,
            sequenceNumber =
                sequenceNumber,
            timestamp =
                clock.millis(),
            correction =
                null
        )
    }

    private suspend fun correctAndPersist(
        userMessage: ChatMessage
    ) {
        try {
            val correction =
                correctLearnerMessage(
                    message =
                        userMessage
                )
                    ?: return

            chatRepository
                .updateMessageCorrection(
                    messageId =
                        correction.messageId,
                    correction =
                        correction.correctedSentence
                )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                SEND_MESSAGE_TAG,
                "Correction could not be persisted.",
                throwable
            )
        }
    }

    private suspend fun assessWithoutBreakingChat(
        userMessage: ChatMessage,
        activeDayCount: Int
    ): String? {
        return try {
            assessChatMessage(
                userMessage =
                    userMessage,
                activeDayCount =
                    activeDayCount
            )

            null
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                SEND_MESSAGE_TAG,
                "CEFR assessment failed.",
                throwable
            )

            ASSESSMENT_WARNING
        }
    }

    private companion object {

        const val SEND_MESSAGE_TAG =
            "SendChatMessage"

        const val ASSESSMENT_WARNING =
            "Your message was sent, but the " +
                    "progress stats could not be updated."
    }
}