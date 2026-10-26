package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFocus
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningObservation
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.LearningEvidenceResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.MasteryState
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.repository.AdaptiveLearningRepository
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFeedback
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFeedbackType
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningRecordResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningTransition

/*
 * Records one assessed learner-message observation in the
 * persistent adaptive-learning memory.
 *
 * Responsibilities:
 *
 * 1. Find an existing focus by its stable focus key.
 * 2. Create the focus when it is first observed.
 * 3. Calculate updated mastery statistics.
 * 4. Save the updated focus and message evidence together.
 * 5. Return an outcome suitable for subtle chat feedback.
 */
class RecordAdaptiveLearningEvidenceUseCase
@Inject constructor(
    private val repository:
    AdaptiveLearningRepository,
    private val masteryCalculator:
    AdaptiveMasteryCalculator,
    private val clock:
    Clock
) {

    suspend operator fun invoke(
        messageId: String,
        conversationId: String?,
        observation: AdaptiveLearningObservation
    ): AdaptiveLearningRecordResult {
        if (messageId.isBlank()) {
            return AdaptiveLearningRecordResult.Skipped(
                reason =
                    "The learner message ID is blank."
            )
        }

        if (!observation.hasUsableEvidence) {
            return AdaptiveLearningRecordResult.Skipped(
                reason =
                    "The observation does not contain usable " +
                            "adaptive-learning evidence."
            )
        }

        val normalizedFocusKey =
            normalizeFocusKey(
                value =
                    observation.focusKey,
                typePrefix =
                    observation
                        .type
                        .name
                        .lowercase()
            )

        if (normalizedFocusKey.isBlank()) {
            return AdaptiveLearningRecordResult.Skipped(
                reason =
                    "The adaptive-learning focus key is blank."
            )
        }

        return try {
            val now =
                clock.millis()

            val existingFocus =
                repository.getFocusByKey(
                    focusKey =
                        normalizedFocusKey
                )

            val baseFocus =
                existingFocus
                    ?: createInitialFocus(
                        focusKey =
                            normalizedFocusKey,
                        observation =
                            observation,
                        observedAt =
                            now
                    )

            /*
             * Keep learner-facing and reinforcement guidance
             * fresh when the evaluator provides a better
             * description of an existing focus.
             */
            val refreshedFocus =
                baseFocus.copy(
                    title =
                        observation
                            .title
                            .trim()
                            .ifBlank {
                                baseFocus.title
                            },
                    description =
                        observation
                            .description
                            .trim()
                            .ifBlank {
                                baseFocus.description
                            },
                    targetCefrLevel =
                        observation.targetCefrLevel,
                    communicationSkill =
                        observation.communicationSkill
                            ?: baseFocus.communicationSkill,
                    reinforcementInstruction =
                        observation
                            .reinforcementInstruction
                            .trim()
                            .ifBlank {
                                baseFocus
                                    .reinforcementInstruction
                            },
                    learnerGuidance =
                        observation
                            .learnerGuidance
                            .trim()
                            .ifBlank {
                                baseFocus.learnerGuidance
                            }
                )

            val updatedFocus =
                masteryCalculator.update(
                    existingFocus =
                        refreshedFocus,
                    result =
                        observation.result,
                    evidenceConfidence =
                        observation
                            .normalizedConfidence,
                    observedAt =
                        now
                )

            repository.recordEvidence(
                focus =
                    updatedFocus,
                messageId =
                    messageId,
                conversationId =
                    conversationId,
                result =
                    observation.result,
                confidence =
                    observation.normalizedConfidence,
                reason =
                    observation.reason,
                learnerText =
                    observation.learnerText,
                correctedText =
                    observation.correctedText,
                levelAppropriateText =
                    observation.levelAppropriateText,
                observedAt =
                    now
            )

            val transition =
                determineTransition(
                    previousState =
                        existingFocus?.masteryState,
                    currentState =
                        updatedFocus.masteryState,
                    evidenceResult =
                        observation.result
                )

            Log.d(
                USE_CASE_TAG,
                buildString {
                    append("Adaptive evidence saved. ")
                    append("focusKey=")
                    append(updatedFocus.focusKey)
                    append(", result=")
                    append(observation.result)
                    append(", mastery=")
                    append(updatedFocus.masteryState)
                    append(", score=")
                    append(updatedFocus.safeMasteryScore)
                    append(", confidence=")
                    append(updatedFocus.safeConfidence)
                }
            )

            AdaptiveLearningRecordResult.Recorded(
                focus =
                    updatedFocus,
                transition =
                    transition,
                feedback =
                    createFeedback(
                        focus =
                            updatedFocus,
                        transition =
                            transition,
                        evidenceResult =
                            observation.result
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
                "Adaptive-learning evidence could not be saved.",
                throwable
            )

            AdaptiveLearningRecordResult.Failed(
                message =
                    throwable.message
                        ?: DEFAULT_SAVE_ERROR
            )
        }
    }

    private fun createInitialFocus(
        focusKey: String,
        observation: AdaptiveLearningObservation,
        observedAt: Long
    ): AdaptiveLearningFocus {
        return AdaptiveLearningFocus(
            id =
                UUID.randomUUID().toString(),
            focusKey =
                focusKey,
            type =
                observation.type,
            title =
                observation.title.trim(),
            description =
                observation.description.trim(),
            targetCefrLevel =
                observation.targetCefrLevel,
            communicationSkill =
                observation.communicationSkill,
            masteryState =
                MasteryState.OBSERVING,
            evidenceCount =
                0,
            struggleCount =
                0,
            successCount =
                0,
            consecutiveSuccessCount =
                0,
            confidence =
                0f,
            masteryScore =
                INITIAL_MASTERY_SCORE,
            reinforcementInstruction =
                observation
                    .reinforcementInstruction
                    .trim(),
            learnerGuidance =
                observation
                    .learnerGuidance
                    .trim(),
            firstObservedAt =
                observedAt,
            lastObservedAt =
                observedAt,
            lastPracticedAt =
                null,
            masteredAt =
                null
        )
    }

    private fun determineTransition(
        previousState: MasteryState?,
        currentState: MasteryState,
        evidenceResult: LearningEvidenceResult
    ): AdaptiveLearningTransition {
        return when {
            previousState == null -> {
                AdaptiveLearningTransition.NEW_FOCUS
            }

            previousState ==
                    MasteryState.MASTERED &&
                    currentState ==
                    MasteryState.NEEDS_REVIEW -> {

                AdaptiveLearningTransition.MASTERY_LOST
            }

            previousState !=
                    MasteryState.MASTERED &&
                    currentState ==
                    MasteryState.MASTERED -> {

                AdaptiveLearningTransition.MASTERED
            }

            previousState !=
                    MasteryState.IMPROVING &&
                    currentState ==
                    MasteryState.IMPROVING -> {

                AdaptiveLearningTransition.IMPROVING
            }

            evidenceResult ==
                    LearningEvidenceResult.STRUGGLED -> {

                AdaptiveLearningTransition.REPEATED_MISTAKE
            }

            evidenceResult ==
                    LearningEvidenceResult.SUCCESS -> {

                AdaptiveLearningTransition.SUCCESSFUL_USE
            }

            evidenceResult ==
                    LearningEvidenceResult.PARTIAL_SUCCESS -> {

                AdaptiveLearningTransition.PARTIAL_SUCCESS
            }

            else -> {
                AdaptiveLearningTransition.NONE
            }
        }
    }

    private fun createFeedback(
        focus: AdaptiveLearningFocus,
        transition: AdaptiveLearningTransition,
        evidenceResult: LearningEvidenceResult
    ): AdaptiveLearningFeedback? {
        return when (transition) {
            AdaptiveLearningTransition.NEW_FOCUS -> {
                if (
                    evidenceResult ==
                    LearningEvidenceResult.STRUGGLED
                ) {
                    AdaptiveLearningFeedback(
                        type =
                            AdaptiveLearningFeedbackType
                                .GENTLE_REMINDER,
                        message =
                            focus.learnerGuidance,
                        focusKey =
                            focus.focusKey
                    )
                } else {
                    null
                }
            }

            AdaptiveLearningTransition.REPEATED_MISTAKE -> {
                AdaptiveLearningFeedback(
                    type =
                        AdaptiveLearningFeedbackType
                            .REPEATED_MISTAKE,
                    message =
                        focus
                            .learnerGuidance
                            .ifBlank {
                                "This is still a useful area " +
                                        "to practice."
                            },
                    focusKey =
                        focus.focusKey
                )
            }

            AdaptiveLearningTransition.PARTIAL_SUCCESS -> {
                AdaptiveLearningFeedback(
                    type =
                        AdaptiveLearningFeedbackType
                            .PARTIAL_SUCCESS,
                    message =
                        "Almost there. Keep practicing " +
                                focus.title.lowercase() +
                                ".",
                    focusKey =
                        focus.focusKey
                )
            }

            AdaptiveLearningTransition.SUCCESSFUL_USE -> {
                if (
                    focus.safeConsecutiveSuccessCount >=
                    MINIMUM_STREAK_FOR_SUCCESS_FEEDBACK
                ) {
                    AdaptiveLearningFeedback(
                        type =
                            AdaptiveLearningFeedbackType
                                .SUCCESS,
                        message =
                            "${focus.title} is becoming more " +
                                    "consistent.",
                        focusKey =
                            focus.focusKey
                    )
                } else {
                    null
                }
            }

            AdaptiveLearningTransition.IMPROVING -> {
                AdaptiveLearningFeedback(
                    type =
                        AdaptiveLearningFeedbackType
                            .IMPROVING,
                    message =
                        "${focus.title}: You're improving.",
                    focusKey =
                        focus.focusKey
                )
            }

            AdaptiveLearningTransition.MASTERED -> {
                AdaptiveLearningFeedback(
                    type =
                        AdaptiveLearningFeedbackType
                            .MASTERED,
                    message =
                        "${focus.title} mastered.",
                    focusKey =
                        focus.focusKey
                )
            }

            AdaptiveLearningTransition.MASTERY_LOST -> {
                AdaptiveLearningFeedback(
                    type =
                        AdaptiveLearningFeedbackType
                            .NEEDS_REVIEW,
                    message =
                        "${focus.title} may need a quick review.",
                    focusKey =
                        focus.focusKey
                )
            }

            AdaptiveLearningTransition.NONE -> {
                null
            }
        }
    }

    private fun normalizeFocusKey(
        value: String,
        typePrefix: String
    ): String {
        var normalizedValue =
            value
                .trim()
                .lowercase()
                .replace(
                    FOCUS_KEY_WHITESPACE_REGEX,
                    "_"
                )
                .replace(
                    FOCUS_KEY_INVALID_CHARACTER_REGEX,
                    ""
                )

        if (normalizedValue.isBlank()) {
            return ""
        }

        /*
         * Add a type prefix when the evaluator supplied only
         * the specific focus name.
         *
         * Example:
         *
         * dative_articles
         *
         * becomes:
         *
         * grammar:dative_articles
         */
        if (
            FOCUS_KEY_TYPE_SEPARATOR !in
            normalizedValue
        ) {
            normalizedValue =
                "$typePrefix$FOCUS_KEY_TYPE_SEPARATOR" +
                        normalizedValue
        }

        return normalizedValue
    }

    private companion object {
        const val USE_CASE_TAG =
            "AdaptiveLearningRecord"

        const val DEFAULT_SAVE_ERROR =
            "Adaptive-learning evidence could not be saved."

        const val INITIAL_MASTERY_SCORE =
            0.50f

        const val MINIMUM_STREAK_FOR_SUCCESS_FEEDBACK =
            2

        const val FOCUS_KEY_TYPE_SEPARATOR =
            ":"

        val FOCUS_KEY_WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )

        val FOCUS_KEY_INVALID_CHARACTER_REGEX =
            Regex(
                "[^a-z0-9:_-]"
            )
    }
}