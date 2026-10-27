package com.julesmeister.deutschcraft.feature.learninganalytics.domain

/*
 * Main learning dimensions used by DeutschCraft analytics.
 *
 * These dimensions can later be connected to:
 *
 * - SkillScoreEntity
 * - AssessmentEvidenceEntity
 * - EvidenceGrammarFeatureEntity
 * - DetectedMistakeEntity
 * - Mission-objective performance
 */
enum class LearningDimension {
    CONVERSATION,
    INTERACTION,
    GRAMMAR,
    VOCABULARY,
    COMPREHENSION
}