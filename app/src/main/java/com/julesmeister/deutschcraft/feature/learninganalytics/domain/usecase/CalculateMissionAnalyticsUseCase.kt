package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import com.julesmeister.deutschcraft.feature.learninganalytics.domain.LearningDimension
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionCompletionEvidence
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionObjectiveEvidence
import javax.inject.Inject
import kotlin.math.roundToInt

/*
 * Calculates analytics for one completed or partially
 * completed mission.
 *
 * The calculation rewards:
 *
 * - Objective completion
 * - Independent communication
 * - Relevant replies
 * - Follow-up questions
 * - Clarification behavior
 * - Correct target-vocabulary usage
 * - Later correction of Besser mistakes
 *
 * It does not treat a mission completion as automatic CEFR
 * mastery.
 */
class CalculateMissionAnalyticsUseCase
@Inject constructor() {

    operator fun invoke(
        evidence:
        MissionCompletionEvidence
    ): MissionAnalytics {
        val objectives =
            evidence.objectiveEvidence

        val completedObjectives =
            objectives.filter { objective ->
                objective.wasCompleted
            }

        val requiredObjectives =
            objectives.filter { objective ->
                objective.isRequired
            }

        val completedRequiredObjectives =
            requiredObjectives.filter { objective ->
                objective.wasCompleted
            }

        val independentlyCompletedObjectives =
            completedObjectives.filter { objective ->
                objective.normalizedHintsUsed ==
                        INDEPENDENT_HINT_COUNT
            }

        val supportedObjectives =
            completedObjectives.filter { objective ->
                objective.normalizedHintsUsed in
                        GENTLE_SUPPORT_HINT_RANGE
            }

        val directlyAssistedObjectives =
            completedObjectives.filter { objective ->
                objective.normalizedHintsUsed >=
                        DIRECT_SUPPORT_HINT_THRESHOLD
            }

        val objectiveCompletionScore =
            calculateObjectiveCompletionScore(
                objectives =
                    objectives
            )

        val independenceScore =
            calculateIndependenceScore(
                objectives =
                    objectives
            )

        val grammarScore =
            calculateGrammarScore(
                learnerMessageCount =
                    evidence.learnerMessageCount,
                correctionCount =
                    evidence.besserCorrectionCount,
                resolvedCorrectionCount =
                    evidence
                        .resolvedBesserCorrectionCount
            )

        val vocabularyScore =
            calculateVocabularyScore(
                availableVocabulary =
                    evidence.availableTargetVocabulary,
                correctlyUsedVocabulary =
                    evidence
                        .correctlyUsedTargetVocabulary
            )

        val comprehensionScore =
            calculateComprehensionScore(
                relevantReplyCount =
                    evidence
                        .contextuallyRelevantReplyCount,
                expectedReplyCount =
                    evidence.expectedReplyCount
            )

        val interactionScore =
            calculateInteractionScore(
                objectiveCompletionScore =
                    objectiveCompletionScore,
                followUpQuestionCount =
                    evidence
                        .independentFollowUpQuestionCount,
                clarificationCount =
                    evidence
                        .successfulClarificationCount,
                comprehensionScore =
                    comprehensionScore
            )

        val overallScore =
            weightedAverage(
                values = listOf(
                    WeightedScore(
                        score =
                            objectiveCompletionScore,
                        weight =
                            OBJECTIVE_COMPLETION_WEIGHT
                    ),
                    WeightedScore(
                        score =
                            independenceScore,
                        weight =
                            INDEPENDENCE_WEIGHT
                    ),
                    WeightedScore(
                        score =
                            grammarScore,
                        weight =
                            GRAMMAR_WEIGHT
                    ),
                    WeightedScore(
                        score =
                            vocabularyScore,
                        weight =
                            VOCABULARY_WEIGHT
                    ),
                    WeightedScore(
                        score =
                            comprehensionScore,
                        weight =
                            COMPREHENSION_WEIGHT
                    ),
                    WeightedScore(
                        score =
                            interactionScore,
                        weight =
                            INTERACTION_WEIGHT
                    )
                )
            )

        val strongObjectiveTitles =
            objectives
                .filter { objective ->
                    objective.wasCompleted &&
                            objective.normalizedHintsUsed <=
                            MAXIMUM_STRONG_OBJECTIVE_HINT_COUNT &&
                            objective
                                .normalizedEvaluationConfidence >=
                            MINIMUM_STRONG_OBJECTIVE_CONFIDENCE
                }
                .map { objective ->
                    objective.title
                }
                .distinct()

        val improvementObjectiveTitles =
            objectives
                .filter { objective ->
                    !objective.wasCompleted ||
                            objective.normalizedHintsUsed >=
                            DIRECT_SUPPORT_HINT_THRESHOLD ||
                            objective
                                .normalizedEvaluationConfidence <
                            MINIMUM_STRONG_OBJECTIVE_CONFIDENCE
                }
                .map { objective ->
                    objective.title
                }
                .distinct()

        return MissionAnalytics(
            missionId =
                evidence.missionId,
            conversationId =
                evidence.conversationId,
            cefrLevel =
                evidence.cefrLevel,
            completedObjectiveCount =
                completedObjectives.size,
            totalObjectiveCount =
                objectives.size,
            completedRequiredObjectiveCount =
                completedRequiredObjectives.size,
            totalRequiredObjectiveCount =
                requiredObjectives.size,
            overallScore =
                overallScore,
            independenceScore =
                independenceScore,
            grammarScore =
                grammarScore,
            vocabularyScore =
                vocabularyScore,
            comprehensionScore =
                comprehensionScore,
            interactionScore =
                interactionScore,
            hintsUsed =
                objectives.sumOf { objective ->
                    objective.normalizedHintsUsed
                },
            besserCorrectionCount =
                evidence.besserCorrectionCount,
            resolvedBesserCorrectionCount =
                evidence
                    .resolvedBesserCorrectionCount,
            independentlyCompletedObjectiveCount =
                independentlyCompletedObjectives.size,
            supportedObjectiveCount =
                supportedObjectives.size,
            directlyAssistedObjectiveCount =
                directlyAssistedObjectives.size,
            strongObjectiveTitles =
                strongObjectiveTitles,
            improvementObjectiveTitles =
                improvementObjectiveTitles,
            correctlyUsedTargetVocabulary =
                evidence
                    .correctlyUsedTargetVocabulary,
            unusedTargetVocabulary =
                evidence
                    .availableTargetVocabulary -
                        evidence
                            .correctlyUsedTargetVocabulary,
            dimensionScores = mapOf(
                LearningDimension.CONVERSATION to
                        objectiveCompletionScore,
                LearningDimension.INTERACTION to
                        interactionScore,
                LearningDimension.GRAMMAR to
                        grammarScore,
                LearningDimension.VOCABULARY to
                        vocabularyScore,
                LearningDimension.COMPREHENSION to
                        comprehensionScore
            )
        )
    }

    private fun calculateObjectiveCompletionScore(
        objectives:
        List<MissionObjectiveEvidence>
    ): Int {
        if (objectives.isEmpty()) {
            return 0
        }

        val completedWeight =
            objectives.sumOf { objective ->
                when {
                    !objective.wasCompleted -> {
                        0.0
                    }

                    objective.isRequired -> {
                        REQUIRED_OBJECTIVE_VALUE
                    }

                    else -> {
                        OPTIONAL_OBJECTIVE_VALUE
                    }
                }
            }

        val availableWeight =
            objectives.sumOf { objective ->
                if (objective.isRequired) {
                    REQUIRED_OBJECTIVE_VALUE
                } else {
                    OPTIONAL_OBJECTIVE_VALUE
                }
            }

        if (availableWeight <= 0.0) {
            return 0
        }

        return percentage(
            numerator =
                completedWeight,
            denominator =
                availableWeight
        )
    }

    private fun calculateIndependenceScore(
        objectives:
        List<MissionObjectiveEvidence>
    ): Int {
        val completedObjectives =
            objectives.filter { objective ->
                objective.wasCompleted
            }

        if (completedObjectives.isEmpty()) {
            return 0
        }

        val independencePoints =
            completedObjectives.sumOf { objective ->
                when (
                    objective.normalizedHintsUsed
                ) {
                    0 -> {
                        INDEPENDENT_OBJECTIVE_POINTS
                    }

                    1 -> {
                        GENTLY_SUPPORTED_OBJECTIVE_POINTS
                    }

                    2 -> {
                        SUBSTANTIALLY_SUPPORTED_OBJECTIVE_POINTS
                    }

                    else -> {
                        DIRECTLY_ASSISTED_OBJECTIVE_POINTS
                    }
                }
            }

        val maximumPoints =
            completedObjectives.size *
                    INDEPENDENT_OBJECTIVE_POINTS

        return percentage(
            numerator =
                independencePoints,
            denominator =
                maximumPoints
        )
    }

    private fun calculateGrammarScore(
        learnerMessageCount: Int,
        correctionCount: Int,
        resolvedCorrectionCount: Int
    ): Int {
        if (learnerMessageCount <= 0) {
            return 0
        }

        val safeCorrectionCount =
            correctionCount.coerceAtLeast(
                0
            )

        val safeResolvedCount =
            resolvedCorrectionCount.coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    safeCorrectionCount
            )

        val unresolvedCorrectionCount =
            safeCorrectionCount -
                    safeResolvedCount

        val unresolvedCorrectionRate =
            unresolvedCorrectionCount
                .toFloat() /
                    learnerMessageCount
                        .coerceAtLeast(
                            1
                        )
                        .toFloat()

        val correctionPenalty =
            (
                    unresolvedCorrectionRate *
                            MAXIMUM_GRAMMAR_PENALTY
                    )
                .coerceAtMost(
                    MAXIMUM_GRAMMAR_PENALTY
                )

        val resolutionBonus =
            if (safeCorrectionCount == 0) {
                NO_CORRECTION_GRAMMAR_BONUS
            } else {
                (
                        safeResolvedCount
                            .toFloat() /
                                safeCorrectionCount
                                    .toFloat() *
                                MAXIMUM_RESOLUTION_BONUS
                        )
            }

        return (
                BASE_GRAMMAR_SCORE -
                        correctionPenalty +
                        resolutionBonus
                )
            .roundToInt()
            .coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    100
            )
    }

    private fun calculateVocabularyScore(
        availableVocabulary: Set<String>,
        correctlyUsedVocabulary: Set<String>
    ): Int {
        val normalizedAvailable =
            availableVocabulary
                .normalizedWords()

        if (normalizedAvailable.isEmpty()) {
            return NEUTRAL_VOCABULARY_SCORE
        }

        val normalizedUsed =
            correctlyUsedVocabulary
                .normalizedWords()

        val correctlyUsedTargetCount =
            normalizedAvailable
                .intersect(
                    normalizedUsed
                )
                .size

        return percentage(
            numerator =
                correctlyUsedTargetCount,
            denominator =
                normalizedAvailable.size
        )
    }

    private fun calculateComprehensionScore(
        relevantReplyCount: Int,
        expectedReplyCount: Int
    ): Int {
        if (expectedReplyCount <= 0) {
            return NEUTRAL_COMPREHENSION_SCORE
        }

        return percentage(
            numerator =
                relevantReplyCount.coerceIn(
                    minimumValue =
                        0,
                    maximumValue =
                        expectedReplyCount
                ),
            denominator =
                expectedReplyCount
        )
    }

    private fun calculateInteractionScore(
        objectiveCompletionScore: Int,
        followUpQuestionCount: Int,
        clarificationCount: Int,
        comprehensionScore: Int
    ): Int {
        val followUpScore =
            (
                    followUpQuestionCount
                        .coerceAtLeast(
                            0
                        ) *
                            FOLLOW_UP_QUESTION_POINTS
                    )
                .coerceAtMost(
                    MAXIMUM_FOLLOW_UP_SCORE
                )

        val clarificationScore =
            (
                    clarificationCount
                        .coerceAtLeast(
                            0
                        ) *
                            CLARIFICATION_POINTS
                    )
                .coerceAtMost(
                    MAXIMUM_CLARIFICATION_SCORE
                )

        return (
                objectiveCompletionScore *
                        INTERACTION_COMPLETION_FACTOR +
                        comprehensionScore *
                        INTERACTION_COMPREHENSION_FACTOR +
                        followUpScore +
                        clarificationScore
                )
            .roundToInt()
            .coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    100
            )
    }

    private fun weightedAverage(
        values: List<WeightedScore>
    ): Int {
        val totalWeight =
            values.sumOf { value ->
                value.weight
            }

        if (totalWeight <= 0.0) {
            return 0
        }

        val weightedTotal =
            values.sumOf { value ->
                value.score
                    .coerceIn(
                        minimumValue =
                            0,
                        maximumValue =
                            100
                    ) *
                        value.weight
            }

        return (
                weightedTotal /
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

    private fun percentage(
        numerator: Int,
        denominator: Int
    ): Int {
        return percentage(
            numerator =
                numerator.toDouble(),
            denominator =
                denominator.toDouble()
        )
    }

    private fun percentage(
        numerator: Double,
        denominator: Double
    ): Int {
        if (denominator <= 0.0) {
            return 0
        }

        return (
                numerator /
                        denominator *
                        100.0
                )
            .roundToInt()
            .coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    100
            )
    }

    private fun Set<String>.normalizedWords():
            Set<String> {
        return asSequence()
            .map { value ->
                value
                    .trim()
                    .lowercase()
            }
            .filter(String::isNotBlank)
            .toSet()
    }

    private data class WeightedScore(
        val score: Int,
        val weight: Double
    )

    private companion object {
        const val REQUIRED_OBJECTIVE_VALUE =
            1.5

        const val OPTIONAL_OBJECTIVE_VALUE =
            1.0

        const val INDEPENDENT_HINT_COUNT =
            0

        val GENTLE_SUPPORT_HINT_RANGE =
            1..1

        const val DIRECT_SUPPORT_HINT_THRESHOLD =
            3

        const val MAXIMUM_STRONG_OBJECTIVE_HINT_COUNT =
            1

        const val MINIMUM_STRONG_OBJECTIVE_CONFIDENCE =
            0.80f

        const val INDEPENDENT_OBJECTIVE_POINTS =
            100

        const val GENTLY_SUPPORTED_OBJECTIVE_POINTS =
            75

        const val SUBSTANTIALLY_SUPPORTED_OBJECTIVE_POINTS =
            45

        const val DIRECTLY_ASSISTED_OBJECTIVE_POINTS =
            20

        const val BASE_GRAMMAR_SCORE =
            82f

        const val MAXIMUM_GRAMMAR_PENALTY =
            70f

        const val MAXIMUM_RESOLUTION_BONUS =
            18f

        const val NO_CORRECTION_GRAMMAR_BONUS =
            12f

        const val NEUTRAL_VOCABULARY_SCORE =
            70

        const val NEUTRAL_COMPREHENSION_SCORE =
            70

        const val FOLLOW_UP_QUESTION_POINTS =
            10

        const val MAXIMUM_FOLLOW_UP_SCORE =
            20

        const val CLARIFICATION_POINTS =
            10

        const val MAXIMUM_CLARIFICATION_SCORE =
            20

        const val INTERACTION_COMPLETION_FACTOR =
            0.35f

        const val INTERACTION_COMPREHENSION_FACTOR =
            0.35f

        const val OBJECTIVE_COMPLETION_WEIGHT =
            0.30

        const val INDEPENDENCE_WEIGHT =
            0.20

        const val GRAMMAR_WEIGHT =
            0.15

        const val VOCABULARY_WEIGHT =
            0.15

        const val COMPREHENSION_WEIGHT =
            0.10

        const val INTERACTION_WEIGHT =
            0.10
    }
}