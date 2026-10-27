package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.CefrProgressSnapshot
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.DimensionProgress
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.LearningDimension
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import javax.inject.Inject
import kotlin.math.roundToInt

/*
 * Produces current-level mastery and next-level readiness
 * using mission-performance evidence.
 *
 * It does not automatically change the learner's official
 * CEFR level.
 *
 * Promotion should require:
 *
 * - Sufficient mission evidence
 * - Balanced performance across dimensions
 * - Strong performance at the next level
 * - A separate reassessment or promotion decision
 */
class CalculateCefrProgressUseCase
@Inject constructor() {

    operator fun invoke(
        currentLevel: CefrLevel,
        missionAnalytics:
        List<MissionAnalytics>
    ): CefrProgressSnapshot {
        val nextLevel =
            currentLevel.next()

        val currentLevelEvidence =
            missionAnalytics
                .filter { analytics ->
                    analytics.cefrLevel ==
                            currentLevel
                }
                .takeLast(
                    MAXIMUM_MISSION_HISTORY_COUNT
                )

        val nextLevelEvidence =
            if (nextLevel == null) {
                emptyList()
            } else {
                missionAnalytics
                    .filter { analytics ->
                        analytics.cefrLevel ==
                                nextLevel
                    }
                    .takeLast(
                        MAXIMUM_MISSION_HISTORY_COUNT
                    )
            }

        val dimensionProgress =
            LearningDimension.entries
                .associateWith { dimension ->
                    val currentDimensionScore =
                        calculateDimensionScore(
                            analytics =
                                currentLevelEvidence,
                            dimension =
                                dimension
                        )

                    val nextDimensionScore =
                        calculateDimensionScore(
                            analytics =
                                nextLevelEvidence,
                            dimension =
                                dimension
                        )

                    DimensionProgress(
                        dimension =
                            dimension,
                        currentLevelScore =
                            currentDimensionScore,
                        nextLevelReadinessScore =
                            calculateEvidenceAdjustedScore(
                                rawScore =
                                    nextDimensionScore,
                                evidenceCount =
                                    nextLevelEvidence.size,
                                minimumEvidenceCount =
                                    MINIMUM_NEXT_LEVEL_MISSION_COUNT
                            ),
                        evidenceCount =
                            currentLevelEvidence.size
                    )
                }

        val currentLevelMastery =
            calculateBalancedMastery(
                dimensionScores =
                    dimensionProgress.values
                        .associate { progress ->
                            progress.dimension to
                                    progress.currentLevelScore
                        },
                evidenceCount =
                    currentLevelEvidence.size,
                minimumEvidenceCount =
                    MINIMUM_CURRENT_LEVEL_MISSION_COUNT
            )

        val nextLevelReadiness =
            if (nextLevel == null) {
                100
            } else {
                calculateBalancedMastery(
                    dimensionScores =
                        dimensionProgress.values
                            .associate { progress ->
                                progress.dimension to
                                        progress
                                            .nextLevelReadinessScore
                            },
                    evidenceCount =
                        nextLevelEvidence.size,
                    minimumEvidenceCount =
                        MINIMUM_NEXT_LEVEL_MISSION_COUNT
                )
            }

        val sortedCurrentDimensions =
            dimensionProgress.values
                .sortedByDescending { progress ->
                    progress.currentLevelScore
                }

        val strongestDimensions =
            sortedCurrentDimensions
                .filter { progress ->
                    progress.currentLevelScore >=
                            STRONG_DIMENSION_THRESHOLD
                }
                .take(
                    MAXIMUM_SUMMARY_DIMENSION_COUNT
                )
                .map { progress ->
                    progress.dimension
                }

        val dimensionsNeedingPractice =
            sortedCurrentDimensions
                .asReversed()
                .filter { progress ->
                    progress.currentLevelScore <
                            PRACTICE_DIMENSION_THRESHOLD
                }
                .take(
                    MAXIMUM_SUMMARY_DIMENSION_COUNT
                )
                .map { progress ->
                    progress.dimension
                }

        val allRequiredNextLevelDimensionsReady =
            nextLevel == null ||
                    dimensionProgress.values.all { progress ->
                        progress.nextLevelReadinessScore >=
                                MINIMUM_REASSESSMENT_DIMENSION_SCORE
                    }

        val reassessmentRecommended =
            nextLevel != null &&
                    currentLevelEvidence.size >=
                    MINIMUM_CURRENT_LEVEL_MISSION_COUNT &&
                    nextLevelEvidence.size >=
                    MINIMUM_NEXT_LEVEL_MISSION_COUNT &&
                    currentLevelMastery >=
                    MINIMUM_REASSESSMENT_MASTERY &&
                    nextLevelReadiness >=
                    MINIMUM_REASSESSMENT_READINESS &&
                    allRequiredNextLevelDimensionsReady

        return CefrProgressSnapshot(
            currentLevel =
                currentLevel,
            nextLevel =
                nextLevel,
            currentLevelMastery =
                currentLevelMastery,
            nextLevelReadiness =
                nextLevelReadiness,
            dimensionProgress =
                dimensionProgress,
            currentLevelMissionCount =
                currentLevelEvidence.size,
            nextLevelMissionCount =
                nextLevelEvidence.size,
            reassessmentRecommended =
                reassessmentRecommended,
            strongestDimensions =
                strongestDimensions,
            dimensionsNeedingPractice =
                dimensionsNeedingPractice
        )
    }

    /*
     * Recent performance receives more weight than older
     * performance.
     *
     * This allows improvement to affect progress while still
     * retaining earlier evidence.
     */
    private fun calculateDimensionScore(
        analytics: List<MissionAnalytics>,
        dimension: LearningDimension
    ): Int {
        if (analytics.isEmpty()) {
            return 0
        }

        val weightedScores =
            analytics.mapIndexed { index, mission ->
                val score =
                    mission.dimensionScores[
                        dimension
                    ]
                        ?: 0

                val recencyPosition =
                    index +
                            1

                val weight =
                    BASE_MISSION_WEIGHT +
                            recencyPosition *
                            RECENCY_WEIGHT_INCREMENT

                WeightedValue(
                    value =
                        score,
                    weight =
                        weight
                )
            }

        val totalWeight =
            weightedScores.sumOf { value ->
                value.weight
            }

        if (totalWeight <= 0.0) {
            return 0
        }

        return (
                weightedScores.sumOf { value ->
                    value.value *
                            value.weight
                } /
                        totalWeight
                )
            .roundToInt()
            .coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    100
            )
    }

    /*
     * Prevents one or two excellent missions from immediately
     * producing 100% readiness.
     */
    private fun calculateEvidenceAdjustedScore(
        rawScore: Int,
        evidenceCount: Int,
        minimumEvidenceCount: Int
    ): Int {
        if (
            rawScore <= 0 ||
            evidenceCount <= 0
        ) {
            return 0
        }

        val evidenceCoverage =
            (
                    evidenceCount
                        .toFloat() /
                            minimumEvidenceCount
                                .coerceAtLeast(
                                    1
                                )
                                .toFloat()
                    )
                .coerceIn(
                    minimumValue =
                        0f,
                    maximumValue =
                        1f
                )

        return (
                rawScore *
                        evidenceCoverage
                )
            .roundToInt()
            .coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    100
            )
    }

    /*
     * Uses the normal average but applies:
     *
     * - Evidence-coverage adjustment
     * - Weakest-dimension penalty
     *
     * This prevents strong vocabulary from hiding very weak
     * interaction or grammar performance.
     */
    private fun calculateBalancedMastery(
        dimensionScores:
        Map<LearningDimension, Int>,
        evidenceCount: Int,
        minimumEvidenceCount: Int
    ): Int {
        if (dimensionScores.isEmpty()) {
            return 0
        }

        val weightedTotal =
            dimensionScores.entries.sumOf { entry ->
                entry.value *
                        dimensionWeight(
                            dimension =
                                entry.key
                        )
            }

        val totalWeight =
            dimensionScores.keys.sumOf { dimension ->
                dimensionWeight(
                    dimension =
                        dimension
                )
            }

        if (totalWeight <= 0.0) {
            return 0
        }

        val averageScore =
            weightedTotal /
                    totalWeight

        val weakestDimensionScore =
            dimensionScores.values
                .minOrNull()
                ?: 0

        val weakestDimensionPenalty =
            if (
                weakestDimensionScore <
                WEAK_DIMENSION_PENALTY_THRESHOLD
            ) {
                (
                        WEAK_DIMENSION_PENALTY_THRESHOLD -
                                weakestDimensionScore
                        ) *
                        WEAK_DIMENSION_PENALTY_FACTOR
            } else {
                0.0
            }

        val evidenceCoverage =
            (
                    evidenceCount
                        .toDouble() /
                            minimumEvidenceCount
                                .coerceAtLeast(
                                    1
                                )
                                .toDouble()
                    )
                .coerceIn(
                    minimumValue =
                        0.0,
                    maximumValue =
                        1.0
                )

        return (
                (
                        averageScore -
                                weakestDimensionPenalty
                        ) *
                        evidenceCoverage
                )
            .roundToInt()
            .coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    100
            )
    }

    private fun dimensionWeight(
        dimension: LearningDimension
    ): Double {
        return when (dimension) {
            LearningDimension.CONVERSATION -> {
                CONVERSATION_WEIGHT
            }

            LearningDimension.INTERACTION -> {
                INTERACTION_WEIGHT
            }

            LearningDimension.GRAMMAR -> {
                GRAMMAR_WEIGHT
            }

            LearningDimension.VOCABULARY -> {
                VOCABULARY_WEIGHT
            }

            LearningDimension.COMPREHENSION -> {
                COMPREHENSION_WEIGHT
            }
        }
    }

    private data class WeightedValue(
        val value: Int,
        val weight: Double
    )

    private companion object {
        const val MAXIMUM_MISSION_HISTORY_COUNT =
            20

        /*
         * Require several current-level missions before the
         * mastery percentage is considered well supported.
         */
        const val MINIMUM_CURRENT_LEVEL_MISSION_COUNT =
            8

        /*
         * Readiness requires direct next-level evidence.
         *
         * Completing only current-level missions cannot prove
         * next-level readiness.
         */
        const val MINIMUM_NEXT_LEVEL_MISSION_COUNT =
            5

        const val BASE_MISSION_WEIGHT =
            1.0

        const val RECENCY_WEIGHT_INCREMENT =
            0.08

        const val WEAK_DIMENSION_PENALTY_THRESHOLD =
            60

        const val WEAK_DIMENSION_PENALTY_FACTOR =
            0.35

        const val STRONG_DIMENSION_THRESHOLD =
            80

        const val PRACTICE_DIMENSION_THRESHOLD =
            65

        const val MAXIMUM_SUMMARY_DIMENSION_COUNT =
            3

        const val MINIMUM_REASSESSMENT_MASTERY =
            80

        const val MINIMUM_REASSESSMENT_READINESS =
            75

        const val MINIMUM_REASSESSMENT_DIMENSION_SCORE =
            65

        const val CONVERSATION_WEIGHT =
            0.25

        const val INTERACTION_WEIGHT =
            0.20

        const val GRAMMAR_WEIGHT =
            0.20

        const val VOCABULARY_WEIGHT =
            0.15

        const val COMPREHENSION_WEIGHT =
            0.20
    }
}