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

            chatRepository.saveMessage(
                message = userMessage
            )

            /*
             * Correction can run while the remaining local
             * progress work is being performed.
             */
            val correctionDeferred =
                async {
                    correctLearnerMessage(
                        message = userMessage
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

            val assistantReply =
                chatRepository
                    .generateAssistantReply(
                        conversationId =
                            conversationId,
                        message =
                            cleanMessage
                    )

            chatRepository.saveMessage(
                message =
                    assistantReply.message
            )

            SendChatMessageResult(
                assistantReply =
                    assistantReply,
                correction =
                    correctionDeferred.await(),
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
                clock.millis()
        )
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