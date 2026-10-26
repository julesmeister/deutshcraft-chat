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
                cleanMessage.isNotBlank()
            ) {
                "The message must not be blank."
            }

            val userMessage =
                createUserMessage(
                    conversationId =
                        conversationId,
                    content =
                        cleanMessage
                )

            /*
             * Save the original learner message first.
             *
             * Room remains the source of truth, so the
             * message becomes visible immediately.
             */
            chatRepository.saveMessage(
                message =
                    userMessage
            )

            /*
             * Record the latest learner activity and replace
             * the existing idle reminder.
             */
            scheduleIdlePrompt()

            /*
             * Correction and assistant generation are
             * independent requests and can run concurrently.
             *
             * The active mission is passed into assistant
             * generation so Emil can temporarily adopt the
             * scenario role and guide the current objective.
             */
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

            /*
             * Record learner activity for progress and streak
             * tracking.
             */
            val activeDayCount =
                activityTracker
                    .recordMessageActivity(
                        profileId =
                            DefaultLearnerProfile
                                .LOCAL_PROFILE_ID
                    )

            /*
             * Assessment is supplementary.
             *
             * A failed assessment produces a warning but does
             * not prevent the assistant from replying.
             */
            val assessmentWarning =
                assessWithoutBreakingChat(
                    userMessage =
                        userMessage,
                    activeDayCount =
                        activeDayCount
                )

            /*
             * Await correction independently.
             */
            correctionDeferred.await()

            val assistantReply =
                assistantReplyDeferred.await()

            /*
             * Save the assistant message after Qwen
             * successfully generates it.
             */
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
            /*
             * Correction is supplementary.
             *
             * A failed correction or Room update must not
             * prevent the assistant from responding.
             */
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