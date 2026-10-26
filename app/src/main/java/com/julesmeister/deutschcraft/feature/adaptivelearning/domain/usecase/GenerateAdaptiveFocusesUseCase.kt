package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.database.entity.DetectedMistakeEntity
import com.julesmeister.deutschcraft.feature.adaptivelearning.data.mapper.DetectedMistakeAdaptiveLearningMapper
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFeedback
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFeedbackType
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFocus
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningMessageResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningObservation
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.CommunicationEvidence
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Main adaptive-learning entry point for one assessed
 * learner message.
 *
 * Combines:
 *
 * - Grammar evidence from DetectedMistakeEntity
 * - Vocabulary evidence from DetectedMistakeEntity
 * - CEFR communication evidence
 *
 * Everything is converted into the same persistent
 * AdaptiveLearningObservation model and recorded together.
 */
class GenerateAdaptiveFocusesUseCase
@Inject constructor(
    private val mistakeMapper:
    DetectedMistakeAdaptiveLearningMapper,
    private val recordObservations:
    RecordAdaptiveLearningObservationsUseCase
) {

    suspend operator fun invoke(
        messageId: String,
        conversationId: String?,
        targetCefrLevel: CefrLevel,
        mistakes:
        List<DetectedMistakeEntity>,
        communicationEvidence:
        List<CommunicationEvidence>
    ): AdaptiveLearningMessageResult {
        if (messageId.isBlank()) {
            return AdaptiveLearningMessageResult(
                skippedObservationCount =
                    mistakes.size +
                            communicationEvidence.size
            )
        }

        return try {
            val mistakeObservations =
                mistakeMapper.mapAll(
                    mistakes =
                        mistakes,
                    targetCefrLevel =
                        targetCefrLevel
                )

            val communicationObservations =
                communicationEvidence
                    .asSequence()
                    .filter { evidence ->
                        evidence.hasUsableEvidence
                    }
                    .map { evidence ->
                        evidence.toAdaptiveLearningObservation()
                    }
                    .toList()

            val combinedObservations =
                combineObservations(
                    mistakeObservations =
                        mistakeObservations,
                    communicationObservations =
                        communicationObservations
                )

            if (combinedObservations.isEmpty()) {
                return AdaptiveLearningMessageResult(
                    skippedObservationCount =
                        mistakes.size +
                                communicationEvidence.size
                )
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
                        combinedObservations
                )

            Log.d(
                USE_CASE_TAG,
                buildString {
                    append("Adaptive focuses generated. ")
                    append("mistakes=")
                    append(mistakes.size)
                    append(", mistakeObservations=")
                    append(mistakeObservations.size)
                    append(", communicationEvidence=")
                    append(communicationEvidence.size)
                    append(", combined=")
                    append(combinedObservations.size)
                    append(", recorded=")
                    append(result.recordedFocusCount)
                }
            )

            result
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                USE_CASE_TAG,
                "Adaptive focuses could not be generated.",
                throwable
            )

            AdaptiveLearningMessageResult(
                failedObservationCount =
                    mistakes.size +
                            communicationEvidence.size
            )
        }
    }

    /*
     * Deduplicates observations and keeps a balanced set.
     *
     * A single message should not create a long list of
     * adaptive focuses.
     *
     * Preferred balance:
     *
     * - Up to one grammar observation
     * - Up to one vocabulary observation
     * - Up to one communication observation
     */
    private fun combineObservations(
        mistakeObservations:
        List<AdaptiveLearningObservation>,
        communicationObservations:
        List<AdaptiveLearningObservation>
    ): List<AdaptiveLearningObservation> {
        val allObservations =
            mistakeObservations +
                    communicationObservations

        return allObservations
            .asSequence()
            .filter { observation ->
                observation.hasUsableEvidence
            }
            .groupBy { observation ->
                observation.normalizedFocusKey
            }
            .mapNotNull { (_, observations) ->
                observations.maxByOrNull { observation ->
                    observation.normalizedConfidence
                }
            }
            .groupBy { observation ->
                observation.type
            }
            .values
            .mapNotNull { sameTypeObservations ->
                sameTypeObservations.maxByOrNull {
                        observation ->

                    observation.normalizedConfidence
                }
            }
            .sortedByDescending { observation ->
                observation.normalizedConfidence
            }
            .take(
                MAXIMUM_FOCUSES_PER_MESSAGE
            )
    }

    /*
     * Can be used when grammar/vocabulary and communication
     * evidence were processed separately but the chat still
     * needs one combined result.
     */
    fun combineResults(
        first:
        AdaptiveLearningMessageResult,
        second:
        AdaptiveLearningMessageResult
    ): AdaptiveLearningMessageResult {
        val recordedFocuses =
            (
                    first.recordedFocuses +
                            second.recordedFocuses
                    )
                .distinctBy { focus ->
                    focus.focusKey
                }

        return AdaptiveLearningMessageResult(
            recordedFocuses =
                recordedFocuses,
            skippedObservationCount =
                first.skippedObservationCount +
                        second.skippedObservationCount,
            failedObservationCount =
                first.failedObservationCount +
                        second.failedObservationCount,
            feedback =
                selectFeedback(
                    first =
                        first.feedback,
                    second =
                        second.feedback
                )
        )
    }

    private fun selectFeedback(
        first: AdaptiveLearningFeedback?,
        second: AdaptiveLearningFeedback?
    ): AdaptiveLearningFeedback? {
        return listOfNotNull(
            first,
            second
        ).maxByOrNull { feedback ->
            feedbackPriority(
                type =
                    feedback.type
            )
        }
    }

    private fun feedbackPriority(
        type: AdaptiveLearningFeedbackType
    ): Int {
        return when (type) {
            AdaptiveLearningFeedbackType.MASTERED -> {
                7
            }

            AdaptiveLearningFeedbackType.NEEDS_REVIEW -> {
                6
            }

            AdaptiveLearningFeedbackType.IMPROVING -> {
                5
            }

            AdaptiveLearningFeedbackType
                .REPEATED_MISTAKE -> {

                4
            }

            AdaptiveLearningFeedbackType.SUCCESS -> {
                3
            }

            AdaptiveLearningFeedbackType
                .PARTIAL_SUCCESS -> {

                2
            }

            AdaptiveLearningFeedbackType
                .GENTLE_REMINDER -> {

                1
            }
        }
    }

    private companion object {
        const val USE_CASE_TAG =
            "GenerateAdaptiveFocuses"

        const val MAXIMUM_FOCUSES_PER_MESSAGE =
            3
    }
}