package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningMessageResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.usecase.RunAdaptiveLearningForMessageUseCase
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Handles adaptive-learning processing for one persisted learner
 * message.
 *
 * Adaptive-learning failure remains supplementary and never
 * fails the normal chat send.
 */
class ChatAdaptiveLearningProcessor
@Inject constructor(
    private val runAdaptiveLearningForMessage:
    RunAdaptiveLearningForMessageUseCase
) {

    suspend fun process(
        learnerMessage: ChatMessage,
        conversationId: String,
        recentMessages: List<ChatMessage>,
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

            logResult(
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
                ChatPresentationTokens.AdaptiveLearningProcessor.ProcessorTag,
                "Adaptive learning could not be completed.",
                throwable
            )

            AdaptiveLearningMessageResult(
                failedObservationCount =
                    1
            )
        }
    }

    private fun logResult(
        learnerMessage: ChatMessage,
        result: AdaptiveLearningMessageResult
    ) {
        Log.d(
            ChatPresentationTokens.AdaptiveLearningProcessor.ProcessorTag,
            buildString {
                append(
                    "Adaptive processing finished. "
                )
                append(
                    "messageId="
                )
                append(
                    learnerMessage.id
                )
                append(
                    ", recorded="
                )
                append(
                    result.recordedFocusCount
                )
                append(
                    ", skipped="
                )
                append(
                    result.skippedObservationCount
                )
                append(
                    ", failed="
                )
                append(
                    result.failedObservationCount
                )
                append(
                    ", feedback="
                )
                append(
                    result.feedback?.type
                )
            }
        )

        result.feedback?.let { feedback ->
            Log.d(
                ChatPresentationTokens.AdaptiveLearningProcessor.FeedbackTag,
                buildString {
                    append(
                        "type="
                    )
                    append(
                        feedback.type
                    )
                    append(
                        ", focus="
                    )
                    append(
                        feedback.focusKey
                    )
                    append(
                        ", message="
                    )
                    append(
                        feedback.message
                    )
                }
            )
        }
    }

}
