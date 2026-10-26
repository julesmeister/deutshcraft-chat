package com.julesmeister.deutschcraft.feature.adaptivelearning.domain

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFeedback
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningMessageResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.usecase.ProcessDetectedMistakesUseCase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Safe integration point between the existing assessment
 * flow and adaptive-learning memory.
 *
 * Adaptive learning is supplementary.
 *
 * If it fails:
 *
 * - The learner message remains saved.
 * - Emil's reply remains successful.
 * - The CEFR assessment remains successful.
 * - The normal chat flow continues.
 */
class AdaptiveLearningAssessmentCoordinator
@Inject constructor(
    private val processDetectedMistakes:
    ProcessDetectedMistakesUseCase
) {

    suspend fun processAssessment(
        evidenceId: String,
        messageId: String,
        conversationId: String?,
        targetCefrLevel: CefrLevel
    ): AdaptiveLearningMessageResult {
        if (
            evidenceId.isBlank() ||
            messageId.isBlank()
        ) {
            return AdaptiveLearningMessageResult()
        }

        return try {
            processDetectedMistakes(
                evidenceId =
                    evidenceId,
                messageId =
                    messageId,
                conversationId =
                    conversationId,
                targetCefrLevel =
                    targetCefrLevel
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                COORDINATOR_TAG,
                "Adaptive learning could not process the " +
                        "completed assessment.",
                throwable
            )

            AdaptiveLearningMessageResult(
                failedObservationCount =
                    1
            )
        }
    }

    suspend fun processAssessmentAndReturnFeedback(
        evidenceId: String,
        messageId: String,
        conversationId: String?,
        targetCefrLevel: CefrLevel
    ): AdaptiveLearningFeedback? {
        return processAssessment(
            evidenceId =
                evidenceId,
            messageId =
                messageId,
            conversationId =
                conversationId,
            targetCefrLevel =
                targetCefrLevel
        ).feedback
    }

    private companion object {
        const val COORDINATOR_TAG =
            "AdaptiveAssessment"
    }
}