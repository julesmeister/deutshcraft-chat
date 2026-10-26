package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFeedback
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFeedbackType
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFocus
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningMessageResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningObservation
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningRecordResult
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Processes every adaptive-learning observation extracted
 * from one learner message.
 *
 * The existing single-observation use case remains
 * responsible for:
 *
 * - Finding or creating the focus
 * - Updating mastery
 * - Saving evidence
 * - Producing focus-level feedback
 *
 * This use case coordinates multiple observations and
 * selects one feedback event for the chat UI.
 */
class RecordAdaptiveLearningObservationsUseCase
@Inject constructor(
    private val recordObservation:
    RecordAdaptiveLearningEvidenceUseCase
) {

    suspend operator fun invoke(
        messageId: String,
        conversationId: String?,
        observations:
        List<AdaptiveLearningObservation>
    ): AdaptiveLearningMessageResult {
        if (messageId.isBlank()) {
            Log.w(
                USE_CASE_TAG,
                "Adaptive observations were skipped because " +
                        "the learner message ID was blank."
            )

            return AdaptiveLearningMessageResult(
                skippedObservationCount =
                    observations.size
            )
        }

        if (observations.isEmpty()) {
            return AdaptiveLearningMessageResult()
        }

        /*
         * A malformed AI result could return the same focus
         * more than once.
         *
         * Keep only the strongest observation for each
         * normalized focus key.
         */
        val uniqueObservations =
            observations
                .asSequence()
                .filter { observation ->
                    observation.hasUsableEvidence
                }
                .groupBy { observation ->
                    observation.normalizedFocusKey
                }
                .mapNotNull { (_, groupedObservations) ->
                    groupedObservations.maxByOrNull {
                            observation ->

                        observation.normalizedConfidence
                    }
                }
                .sortedWith(
                    compareByDescending<
                            AdaptiveLearningObservation
                            > { observation ->

                        observationPriority(
                            observation =
                                observation
                        )
                    }
                        .thenByDescending { observation ->
                            observation.normalizedConfidence
                        }
                )
                .take(
                    MAXIMUM_OBSERVATIONS_PER_MESSAGE
                )

        if (uniqueObservations.isEmpty()) {
            return AdaptiveLearningMessageResult(
                skippedObservationCount =
                    observations.size
            )
        }

        val recordedFocuses =
            mutableListOf<AdaptiveLearningFocus>()

        val feedbackCandidates =
            mutableListOf<AdaptiveLearningFeedback>()

        var skippedCount =
            observations.size -
                    uniqueObservations.size

        var failedCount =
            0

        for (observation in uniqueObservations) {
            val recordResult =
                try {
                    recordObservation(
                        messageId =
                            messageId,
                        conversationId =
                            conversationId,
                        observation =
                            observation
                    )
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    Log.e(
                        USE_CASE_TAG,
                        "Unexpected failure while recording " +
                                "focus " +
                                observation.normalizedFocusKey,
                        throwable
                    )

                    failedCount +=
                        1

                    continue
                }

            when (recordResult) {
                is AdaptiveLearningRecordResult.Recorded -> {
                    recordedFocuses +=
                        recordResult.focus

                    recordResult
                        .feedback
                        ?.takeIf { feedback ->
                            feedback.isUsable
                        }
                        ?.let(
                            feedbackCandidates::add
                        )
                }

                is AdaptiveLearningRecordResult.Skipped -> {
                    skippedCount +=
                        1

                    Log.d(
                        USE_CASE_TAG,
                        "Adaptive observation skipped: " +
                                recordResult.reason
                    )
                }

                is AdaptiveLearningRecordResult.Failed -> {
                    failedCount +=
                        1

                    Log.e(
                        USE_CASE_TAG,
                        "Adaptive observation failed: " +
                                recordResult.message
                    )
                }
            }
        }

        val selectedFeedback =
            selectFeedback(
                candidates =
                    feedbackCandidates
            )

        Log.d(
            USE_CASE_TAG,
            buildString {
                append("Adaptive-learning message processed. ")
                append("recorded=")
                append(recordedFocuses.size)
                append(", skipped=")
                append(skippedCount)
                append(", failed=")
                append(failedCount)
                append(", feedback=")
                append(selectedFeedback?.type)
            }
        )

        return AdaptiveLearningMessageResult(
            recordedFocuses =
                recordedFocuses,
            skippedObservationCount =
                skippedCount,
            failedObservationCount =
                failedCount,
            feedback =
                selectedFeedback
        )
    }

    /*
     * The chat should show only one adaptive-learning event
     * after a message.
     *
     * Achievement and regression events outrank ordinary
     * reminders.
     */
    private fun selectFeedback(
        candidates:
        List<AdaptiveLearningFeedback>
    ): AdaptiveLearningFeedback? {
        return candidates.maxByOrNull { feedback ->
            feedbackPriority(
                type =
                    feedback.type
            )
        }
    }

    private fun observationPriority(
        observation: AdaptiveLearningObservation
    ): Int {
        return when (
            observation.result
        ) {
            com.julesmeister.deutschcraft
                .feature
                .adaptivelearning
                .domain
                .model
                .LearningEvidenceResult
                .STRUGGLED -> {

                STRUGGLED_OBSERVATION_PRIORITY
            }

            com.julesmeister.deutschcraft
                .feature
                .adaptivelearning
                .domain
                .model
                .LearningEvidenceResult
                .SUCCESS -> {

                SUCCESS_OBSERVATION_PRIORITY
            }

            com.julesmeister.deutschcraft
                .feature
                .adaptivelearning
                .domain
                .model
                .LearningEvidenceResult
                .PARTIAL_SUCCESS -> {

                PARTIAL_OBSERVATION_PRIORITY
            }

            com.julesmeister.deutschcraft
                .feature
                .adaptivelearning
                .domain
                .model
                .LearningEvidenceResult
                .NOT_OBSERVED -> {

                NOT_OBSERVED_PRIORITY
            }
        }
    }

    private fun feedbackPriority(
        type: AdaptiveLearningFeedbackType
    ): Int {
        return when (type) {
            AdaptiveLearningFeedbackType.MASTERED -> {
                MASTERED_FEEDBACK_PRIORITY
            }

            AdaptiveLearningFeedbackType.NEEDS_REVIEW -> {
                NEEDS_REVIEW_FEEDBACK_PRIORITY
            }

            AdaptiveLearningFeedbackType.IMPROVING -> {
                IMPROVING_FEEDBACK_PRIORITY
            }

            AdaptiveLearningFeedbackType
                .REPEATED_MISTAKE -> {

                REPEATED_MISTAKE_FEEDBACK_PRIORITY
            }

            AdaptiveLearningFeedbackType.SUCCESS -> {
                SUCCESS_FEEDBACK_PRIORITY
            }

            AdaptiveLearningFeedbackType
                .PARTIAL_SUCCESS -> {

                PARTIAL_SUCCESS_FEEDBACK_PRIORITY
            }

            AdaptiveLearningFeedbackType
                .GENTLE_REMINDER -> {

                GENTLE_REMINDER_FEEDBACK_PRIORITY
            }
        }
    }

    private companion object {
        const val USE_CASE_TAG =
            "AdaptiveLearningBatch"

        /*
         * Prevent one learner message from creating a large
         * number of low-value learning focuses.
         *
         * One grammar, vocabulary, and communication focus
         * is already enough evidence from a single turn.
         */
        const val MAXIMUM_OBSERVATIONS_PER_MESSAGE =
            3

        const val STRUGGLED_OBSERVATION_PRIORITY =
            4

        const val SUCCESS_OBSERVATION_PRIORITY =
            3

        const val PARTIAL_OBSERVATION_PRIORITY =
            2

        const val NOT_OBSERVED_PRIORITY =
            1

        const val MASTERED_FEEDBACK_PRIORITY =
            7

        const val NEEDS_REVIEW_FEEDBACK_PRIORITY =
            6

        const val IMPROVING_FEEDBACK_PRIORITY =
            5

        const val REPEATED_MISTAKE_FEEDBACK_PRIORITY =
            4

        const val SUCCESS_FEEDBACK_PRIORITY =
            3

        const val PARTIAL_SUCCESS_FEEDBACK_PRIORITY =
            2

        const val GENTLE_REMINDER_FEEDBACK_PRIORITY =
            1
    }
}