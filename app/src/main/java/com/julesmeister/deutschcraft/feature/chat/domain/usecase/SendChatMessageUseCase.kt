package com.julesmeister.deutschcraft.feature.chat.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.activity.ActivityTracker
import com.julesmeister.deutschcraft.core.cefr.model.DefaultLearnerProfile
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
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
    private val clock:
    Clock
) {

    suspend operator fun invoke(
        conversationId: String,
        messageText: String
    ): SendChatMessageResult =
        coroutineScope {
            val cleanMessage =
                messageText.trim()

            require(cleanMessage.isNotBlank()) {
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
             * It immediately becomes visible because Room
             * is the message source of truth.
             */
            chatRepository.saveMessage(
                message = userMessage
            )

            /*
             * Correction and assistant generation can run
             * concurrently because they are independent
             * Qwen requests.
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
                                cleanMessage
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

            /*
             * Await correction independently.
             *
             * updateMessageCorrection() changes the Room
             * row, causing the existing message flow to
             * emit the expanded Besser bubble.
             */
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
                ) ?: return

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
             * A failed correction must not prevent the
             * assistant from responding.
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