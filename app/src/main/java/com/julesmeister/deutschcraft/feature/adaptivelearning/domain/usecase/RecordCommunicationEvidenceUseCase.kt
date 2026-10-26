package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningMessageResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.CommunicationEvidence
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Records communication-skill evidence for one learner
 * message.
 *
 * A message may demonstrate several CEFR communication
 * behaviors.
 *
 * Example:
 *
 * "Ich mag Berlin, weil die Stadt sehr vielfältig ist."
 *
 * Possible evidence:
 *
 * EXPRESSING_OPINIONS = SUCCESS
 * GIVING_REASONS = SUCCESS
 * CONNECTING_IDEAS = SUCCESS
 *
 * The batch recorder limits how many focuses one message can
 * update and selects only one subtle learner-facing feedback
 * event.
 */
class RecordCommunicationEvidenceUseCase
@Inject constructor(
    private val recordObservations:
    RecordAdaptiveLearningObservationsUseCase
) {

    suspend operator fun invoke(
        messageId: String,
        conversationId: String?,
        evidence:
        List<CommunicationEvidence>
    ): AdaptiveLearningMessageResult {
        if (messageId.isBlank()) {
            Log.w(
                USE_CASE_TAG,
                "Communication evidence was skipped because " +
                        "the learner message ID was blank."
            )

            return AdaptiveLearningMessageResult(
                skippedObservationCount =
                    evidence.size
            )
        }

        if (evidence.isEmpty()) {
            return AdaptiveLearningMessageResult()
        }

        return try {
            val usefulEvidence =
                evidence
                    .asSequence()
                    .filter { item ->
                        item.hasUsableEvidence
                    }
                    /*
                     * If the evaluator returned the same skill
                     * more than once, retain the strongest
                     * classification.
                     */
                    .groupBy { item ->
                        item.focusKey
                    }
                    .mapNotNull { (_, groupedEvidence) ->
                        groupedEvidence.maxByOrNull { item ->
                            item.normalizedConfidence
                        }
                    }
                    .sortedByDescending { item ->
                        item.normalizedConfidence
                    }
                    .take(
                        MAXIMUM_COMMUNICATION_EVIDENCE_PER_MESSAGE
                    )

            if (usefulEvidence.isEmpty()) {
                return AdaptiveLearningMessageResult(
                    skippedObservationCount =
                        evidence.size
                )
            }

            val observations =
                usefulEvidence.map { item ->
                    item.toAdaptiveLearningObservation()
                }

            val result =
                recordObservations(
                    messageId =
                        messageId.trim(),
                    conversationId =
                        conversationId
                            ?.trim()
                            ?.takeIf(
                                String::isNotBlank
                            ),
                    observations =
                        observations
                )

            Log.d(
                USE_CASE_TAG,
                buildString {
                    append("Communication evidence processed. ")
                    append("input=")
                    append(evidence.size)
                    append(", useful=")
                    append(usefulEvidence.size)
                    append(", recorded=")
                    append(result.recordedFocusCount)
                    append(", feedback=")
                    append(result.feedback?.type)
                }
            )

            result.copy(
                skippedObservationCount =
                    result.skippedObservationCount +
                            (
                                    evidence.size -
                                            usefulEvidence.size
                                    )
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                USE_CASE_TAG,
                "Communication evidence could not be recorded.",
                throwable
            )

            AdaptiveLearningMessageResult(
                failedObservationCount =
                    evidence.size
            )
        }
    }

    private companion object {
        const val USE_CASE_TAG =
            "CommunicationEvidence"

        /*
         * One message should not update every communication
         * skill at once.
         *
         * Keep only the strongest meaningful observations.
         */
        const val MAXIMUM_COMMUNICATION_EVIDENCE_PER_MESSAGE =
            3
    }
}