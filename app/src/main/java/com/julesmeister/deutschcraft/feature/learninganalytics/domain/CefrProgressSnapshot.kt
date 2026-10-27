package com.julesmeister.deutschcraft.feature.learninganalytics.domain

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

/*
 * Current CEFR learning-progress snapshot.
 *
 * currentLevelMastery:
 *
 * How consistently the learner demonstrates the abilities
 * expected at their current CEFR level.
 *
 * nextLevelReadiness:
 *
 * How much reliable evidence DeutschCraft has that the
 * learner can already perform at the next CEFR level.
 *
 * These are intentionally separate.
 */
data class CefrProgressSnapshot(
    val currentLevel: CefrLevel,
    val nextLevel: CefrLevel?,

    val currentLevelMastery: Int,
    val nextLevelReadiness: Int,

    val dimensionProgress:
    Map<LearningDimension, DimensionProgress>,

    /*
     * Number of missions used as evidence for the current
     * level.
     */
    val currentLevelMissionCount: Int,

    /*
     * Number of missions used as evidence for the next level.
     */
    val nextLevelMissionCount: Int,

    /*
     * Whether enough balanced evidence exists to consider
     * reassessing the learner's official CEFR level.
     *
     * This does not automatically promote the learner.
     */
    val reassessmentRecommended: Boolean,

    val strongestDimensions:
    List<LearningDimension>,

    val dimensionsNeedingPractice:
    List<LearningDimension>
)

data class DimensionProgress(
    val dimension: LearningDimension,
    val currentLevelScore: Int,
    val nextLevelReadinessScore: Int,
    val evidenceCount: Int
)