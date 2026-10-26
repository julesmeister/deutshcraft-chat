package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.usecase

import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFocus
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.LearningEvidenceResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.MasteryState
import javax.inject.Inject
import kotlin.math.max

/*
 * Calculates updated mastery statistics after one new piece
 * of learner evidence.
 *
 * This class is deterministic and contains no Room or AI
 * dependencies, making it easy to unit test.
 */
class AdaptiveMasteryCalculator @Inject constructor() {

    fun update(
        existingFocus: AdaptiveLearningFocus,
        result: LearningEvidenceResult,
        evidenceConfidence: Float,
        observedAt: Long
    ): AdaptiveLearningFocus {
        if (
            result ==
            LearningEvidenceResult.NOT_OBSERVED
        ) {
            return existingFocus
        }

        val safeConfidence =
            evidenceConfidence.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            )

        val updatedEvidenceCount =
            existingFocus.safeEvidenceCount + 1

        val updatedStruggleCount =
            when (result) {
                LearningEvidenceResult.STRUGGLED -> {
                    existingFocus.safeStruggleCount + 1
                }

                else -> {
                    existingFocus.safeStruggleCount
                }
            }

        val updatedSuccessCount =
            when (result) {
                LearningEvidenceResult.SUCCESS -> {
                    existingFocus.safeSuccessCount + 1
                }

                LearningEvidenceResult.PARTIAL_SUCCESS -> {
                    /*
                     * Partial success contributes evidence but
                     * does not count as a complete successful
                     * demonstration.
                     */
                    existingFocus.safeSuccessCount
                }

                else -> {
                    existingFocus.safeSuccessCount
                }
            }

        val updatedConsecutiveSuccessCount =
            when (result) {
                LearningEvidenceResult.SUCCESS -> {
                    existingFocus
                        .safeConsecutiveSuccessCount + 1
                }

                LearningEvidenceResult.PARTIAL_SUCCESS -> {
                    /*
                     * Preserve part of the learner's momentum,
                     * but do not increase the streak.
                     */
                    existingFocus
                        .safeConsecutiveSuccessCount
                        .coerceAtMost(
                            maximumValue =
                                PARTIAL_SUCCESS_MAXIMUM_STREAK
                        )
                }

                LearningEvidenceResult.STRUGGLED -> {
                    0
                }

                LearningEvidenceResult.NOT_OBSERVED -> {
                    existingFocus
                        .safeConsecutiveSuccessCount
                }
            }

        val evidenceValue =
            evidenceValueFor(
                result =
                    result
            )

        val updatedMasteryScore =
            calculateMasteryScore(
                previousMasteryScore =
                    existingFocus.safeMasteryScore,
                evidenceValue =
                    evidenceValue,
                evidenceConfidence =
                    safeConfidence,
                evidenceCount =
                    updatedEvidenceCount
            )

        val updatedPatternConfidence =
            calculatePatternConfidence(
                previousConfidence =
                    existingFocus.safeConfidence,
                evidenceConfidence =
                    safeConfidence,
                evidenceCount =
                    updatedEvidenceCount
            )

        val updatedMasteryState =
            determineMasteryState(
                previousState =
                    existingFocus.masteryState,
                result =
                    result,
                evidenceCount =
                    updatedEvidenceCount,
                struggleCount =
                    updatedStruggleCount,
                successCount =
                    updatedSuccessCount,
                consecutiveSuccessCount =
                    updatedConsecutiveSuccessCount,
                masteryScore =
                    updatedMasteryScore,
                patternConfidence =
                    updatedPatternConfidence
            )

        val newlyMastered =
            updatedMasteryState ==
                    MasteryState.MASTERED &&
                    existingFocus.masteryState !=
                    MasteryState.MASTERED

        val masteryLost =
            existingFocus.masteryState ==
                    MasteryState.MASTERED &&
                    updatedMasteryState !=
                    MasteryState.MASTERED

        return existingFocus.copy(
            masteryState =
                updatedMasteryState,
            evidenceCount =
                updatedEvidenceCount,
            struggleCount =
                updatedStruggleCount,
            successCount =
                updatedSuccessCount,
            consecutiveSuccessCount =
                updatedConsecutiveSuccessCount,
            confidence =
                updatedPatternConfidence,
            masteryScore =
                updatedMasteryScore,
            lastObservedAt =
                max(
                    existingFocus.lastObservedAt,
                    observedAt
                ),
            lastPracticedAt =
                observedAt,
            masteredAt =
                when {
                    newlyMastered -> {
                        observedAt
                    }

                    masteryLost -> {
                        null
                    }

                    else -> {
                        existingFocus.masteredAt
                    }
                }
        )
    }

    private fun calculateMasteryScore(
        previousMasteryScore: Float,
        evidenceValue: Float,
        evidenceConfidence: Float,
        evidenceCount: Int
    ): Float {
        /*
         * Early observations move mastery faster because the
         * system has little existing evidence.
         *
         * As more evidence accumulates, individual messages
         * have progressively less influence.
         */
        val learningRate =
            when {
                evidenceCount <=
                        EARLY_EVIDENCE_LIMIT -> {

                    EARLY_LEARNING_RATE
                }

                evidenceCount <=
                        ESTABLISHED_EVIDENCE_LIMIT -> {

                    ESTABLISHED_LEARNING_RATE
                }

                else -> {
                    MATURE_LEARNING_RATE
                }
            }

        val confidenceAdjustedRate =
            learningRate *
                    evidenceConfidence.coerceAtLeast(
                        minimumValue =
                            MINIMUM_EVIDENCE_INFLUENCE
                    )

        return (
                previousMasteryScore +
                        (
                                evidenceValue -
                                        previousMasteryScore
                                ) *
                        confidenceAdjustedRate
                )
            .coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            )
    }

    private fun calculatePatternConfidence(
        previousConfidence: Float,
        evidenceConfidence: Float,
        evidenceCount: Int
    ): Float {
        /*
         * Pattern confidence grows as independent message
         * evidence accumulates.
         */
        val evidenceCoverage =
            (
                    evidenceCount.toFloat() /
                            EVIDENCE_COUNT_FOR_FULL_CONFIDENCE
                    )
                .coerceIn(
                    minimumValue =
                        0f,
                    maximumValue =
                        1f
                )

        val combinedConfidence =
            (
                    previousConfidence *
                            PREVIOUS_CONFIDENCE_WEIGHT
                    ) +
                    (
                            evidenceConfidence *
                                    NEW_CONFIDENCE_WEIGHT
                            )

        return (
                combinedConfidence *
                        EVIDENCE_CONFIDENCE_WEIGHT +
                        evidenceCoverage *
                        EVIDENCE_COVERAGE_WEIGHT
                )
            .coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            )
    }

    private fun determineMasteryState(
        previousState: MasteryState,
        result: LearningEvidenceResult,
        evidenceCount: Int,
        struggleCount: Int,
        successCount: Int,
        consecutiveSuccessCount: Int,
        masteryScore: Float,
        patternConfidence: Float
    ): MasteryState {
        /*
         * A mastered behavior that reappears as a confident
         * mistake should move into review rather than being
         * treated as completely unlearned.
         */
        if (
            previousState ==
            MasteryState.MASTERED &&
            result ==
            LearningEvidenceResult.STRUGGLED &&
            patternConfidence >=
            REVIEW_CONFIDENCE_THRESHOLD
        ) {
            return MasteryState.NEEDS_REVIEW
        }

        if (
            consecutiveSuccessCount >=
            REQUIRED_CONSECUTIVE_SUCCESSES_FOR_MASTERY &&
            successCount >=
            REQUIRED_TOTAL_SUCCESSES_FOR_MASTERY &&
            masteryScore >=
            MASTERY_SCORE_THRESHOLD &&
            patternConfidence >=
            MASTERY_CONFIDENCE_THRESHOLD
        ) {
            return MasteryState.MASTERED
        }

        if (
            evidenceCount <
            MINIMUM_EVIDENCE_FOR_CLASSIFICATION
        ) {
            return MasteryState.OBSERVING
        }

        val struggleRate =
            struggleCount.toFloat() /
                    evidenceCount.toFloat()

        val successRate =
            successCount.toFloat() /
                    evidenceCount.toFloat()

        if (
            struggleRate >=
            STRUGGLING_RATE_THRESHOLD &&
            patternConfidence >=
            STRUGGLING_CONFIDENCE_THRESHOLD
        ) {
            return MasteryState.STRUGGLING
        }

        if (
            consecutiveSuccessCount >=
            MINIMUM_IMPROVING_SUCCESS_STREAK &&
            successRate >=
            IMPROVING_SUCCESS_RATE_THRESHOLD
        ) {
            return MasteryState.IMPROVING
        }

        return when (result) {
            LearningEvidenceResult.STRUGGLED -> {
                MasteryState.PRACTICING
            }

            LearningEvidenceResult.PARTIAL_SUCCESS -> {
                MasteryState.PRACTICING
            }

            LearningEvidenceResult.SUCCESS -> {
                MasteryState.IMPROVING
            }

            LearningEvidenceResult.NOT_OBSERVED -> {
                previousState
            }
        }
    }

    private fun evidenceValueFor(
        result: LearningEvidenceResult
    ): Float {
        return when (result) {
            LearningEvidenceResult.STRUGGLED -> {
                STRUGGLE_EVIDENCE_VALUE
            }

            LearningEvidenceResult.PARTIAL_SUCCESS -> {
                PARTIAL_SUCCESS_EVIDENCE_VALUE
            }

            LearningEvidenceResult.SUCCESS -> {
                SUCCESS_EVIDENCE_VALUE
            }

            LearningEvidenceResult.NOT_OBSERVED -> {
                NOT_OBSERVED_EVIDENCE_VALUE
            }
        }
    }

    private companion object {
        const val PARTIAL_SUCCESS_MAXIMUM_STREAK =
            1

        const val EARLY_EVIDENCE_LIMIT =
            3

        const val ESTABLISHED_EVIDENCE_LIMIT =
            10

        const val EARLY_LEARNING_RATE =
            0.45f

        const val ESTABLISHED_LEARNING_RATE =
            0.28f

        const val MATURE_LEARNING_RATE =
            0.16f

        const val MINIMUM_EVIDENCE_INFLUENCE =
            0.35f

        const val EVIDENCE_COUNT_FOR_FULL_CONFIDENCE =
            12f

        const val PREVIOUS_CONFIDENCE_WEIGHT =
            0.65f

        const val NEW_CONFIDENCE_WEIGHT =
            0.35f

        const val EVIDENCE_CONFIDENCE_WEIGHT =
            0.72f

        const val EVIDENCE_COVERAGE_WEIGHT =
            0.28f

        const val REQUIRED_CONSECUTIVE_SUCCESSES_FOR_MASTERY =
            4

        const val REQUIRED_TOTAL_SUCCESSES_FOR_MASTERY =
            6

        const val MINIMUM_EVIDENCE_FOR_CLASSIFICATION =
            3

        const val MINIMUM_IMPROVING_SUCCESS_STREAK =
            2

        const val MASTERY_SCORE_THRESHOLD =
            0.82f

        const val MASTERY_CONFIDENCE_THRESHOLD =
            0.72f

        const val REVIEW_CONFIDENCE_THRESHOLD =
            0.70f

        const val STRUGGLING_RATE_THRESHOLD =
            0.55f

        const val STRUGGLING_CONFIDENCE_THRESHOLD =
            0.55f

        const val IMPROVING_SUCCESS_RATE_THRESHOLD =
            0.50f

        const val STRUGGLE_EVIDENCE_VALUE =
            0f

        const val PARTIAL_SUCCESS_EVIDENCE_VALUE =
            0.55f

        const val SUCCESS_EVIDENCE_VALUE =
            1f

        const val NOT_OBSERVED_EVIDENCE_VALUE =
            0.5f
    }
}