package com.julesmeister.deutschcraft.feature.adaptivelearning.domain

import android.util.Log
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFeedback
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningMessageResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningObservation
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.usecase.RecordAdaptiveLearningObservationsUseCase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Safe entry point for recording adaptive-learning
 * observations after normal message assessment.
 *
 * Adaptive learning is supplementary:
 *
 * - The learner message may already be saved.
 * - Emil's response may already be visible.
 * - Assessment may already be complete.
 *
 * An adaptive-learning failure must not turn that successful
 * chat operation into an error.
 */
class AdaptiveLearningCoordinator
@Inject constructor(
    private val recordObservations:
    RecordAdaptiveLearningObservationsUseCase
) {

    suspend fun process(
        messageId: String,
        conversationId: String?,
        observations:
        List<AdaptiveLearningObservation>
    ): AdaptiveLearningMessageResult {
        if (
            messageId.isBlank() ||
            observations.isEmpty()
        ) {
            return AdaptiveLearningMessageResult()
        }

        return try {
            recordObservations(
                messageId =
                    messageId,
                conversationId =
                    conversationId,
                observations =
                    observations
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                COORDINATOR_TAG,
                "Adaptive-learning processing failed.",
                throwable
            )

            AdaptiveLearningMessageResult(
                failedObservationCount =
                    observations.size
            )
        }
    }

    suspend fun processAndReturnFeedback(
        messageId: String,
        conversationId: String?,
        observations:
        List<AdaptiveLearningObservation>
    ): AdaptiveLearningFeedback? {
        return process(
            messageId =
                messageId,
            conversationId =
                conversationId,
            observations =
                observations
        ).feedback
    }

    private companion object {
        const val COORDINATOR_TAG =
            "AdaptiveLearning"
    }
}