package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.AdaptiveLearningChatCoordinator
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningMessageResult
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Simple facade used by the chat pipeline after normal
 * assessment persistence has completed.
 */
class RunAdaptiveLearningForMessageUseCase
@Inject constructor(
    private val getDetectedMistakes:
    GetDetectedMistakesForMessageUseCase,
    private val adaptiveLearningCoordinator:
    AdaptiveLearningChatCoordinator
) {

    suspend operator fun invoke(
        learnerMessage: ChatMessage,
        conversationId: String,
        cefrLevel: CefrLevel,
        recentMessages: List<ChatMessage>
    ): AdaptiveLearningMessageResult {
        return try {
            val mistakes =
                getDetectedMistakes(
                    messageId =
                        learnerMessage.id
                )

            adaptiveLearningCoordinator.process(
                learnerMessage =
                    learnerMessage,
                conversationId =
                    conversationId,
                cefrLevel =
                    cefrLevel,
                recentMessages =
                    recentMessages,
                detectedMistakes =
                    mistakes
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                USE_CASE_TAG,
                "Adaptive learning could not run for learner " +
                        "message ${learnerMessage.id}.",
                throwable
            )

            AdaptiveLearningMessageResult(
                failedObservationCount =
                    1
            )
        }
    }

    private companion object {
        const val USE_CASE_TAG =
            "RunAdaptiveLearning"
    }
}