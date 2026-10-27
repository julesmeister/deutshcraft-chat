package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionMessageInteractionEvidence
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionInteractionEvidenceRepository
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionInteractionEvaluation
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Persists one interaction result from the existing mission
 * evaluation Qwen call.
 *
 * Failure is supplementary and does not prevent objective
 * completion.
 */
class RecordMissionInteractionEvidenceUseCase
@Inject constructor(
    private val repository:
    MissionInteractionEvidenceRepository,
    private val clock:
    Clock
) {

    suspend operator fun invoke(
        conversationMissionId: String,
        messageId: String,
        evaluation:
        MissionInteractionEvaluation
    ): Boolean {
        require(
            conversationMissionId.isNotBlank()
        ) {
            "Conversation mission ID must not be blank."
        }

        require(
            messageId.isNotBlank()
        ) {
            "Message ID must not be blank."
        }

        /*
         * EMPTY means interaction classification was not
         * available, such as a failed Qwen request.
         *
         * Do not persist missing evidence as learner failure.
         */
        if (
            evaluation.confidence <=
            NO_EVIDENCE_CONFIDENCE
        ) {
            return false
        }

        return try {
            repository.save(
                evidence =
                    MissionMessageInteractionEvidence(
                        conversationMissionId =
                            conversationMissionId,
                        messageId =
                            messageId,
                        relevantReply =
                            evaluation.relevantReply,
                        followUpQuestion =
                            evaluation.followUpQuestion,
                        clarification =
                            evaluation.clarification,
                        replyExpected =
                            evaluation.replyExpected,
                        confidence =
                            evaluation.normalizedConfidence,
                        evaluatedAt =
                            clock.millis()
                    )
            )

            true
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                USE_CASE_TAG,
                "Mission interaction evidence could not be " +
                        "saved.",
                throwable
            )

            false
        }
    }

    private companion object {
        const val USE_CASE_TAG =
            "RecordMissionInteraction"

        const val NO_EVIDENCE_CONFIDENCE =
            0f
    }
}