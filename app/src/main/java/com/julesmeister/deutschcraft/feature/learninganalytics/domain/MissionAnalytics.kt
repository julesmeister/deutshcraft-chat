package com.julesmeister.deutschcraft.feature.learninganalytics.domain

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

/*
 * Learner-facing analytics produced after one mission.
 *
 * Every score uses a 0 to 100 scale.
 */
data class MissionAnalytics(
    val missionId: String,
    val conversationId: String,
    val cefrLevel: CefrLevel,

    val completedObjectiveCount: Int,
    val totalObjectiveCount: Int,
    val completedRequiredObjectiveCount: Int,
    val totalRequiredObjectiveCount: Int,

    /*
     * Overall mission-performance score.
     */
    val overallScore: Int,

    /*
     * Ability to complete the conversation without direct
     * hints or supplied example phrases.
     */
    val independenceScore: Int,

    /*
     * Mission grammar performance inferred from Besser
     * correction frequency and later resolution.
     */
    val grammarScore: Int,

    /*
     * Correct use of mission target vocabulary.
     */
    val vocabularyScore: Int,

    /*
     * Ability to respond relevantly and understand the
     * assistant's conversational intent.
     */
    val comprehensionScore: Int,

    /*
     * Follow-up questions, clarification, and interactive
     * conversation behavior.
     */
    val interactionScore: Int,

    val hintsUsed: Int,
    val besserCorrectionCount: Int,
    val resolvedBesserCorrectionCount: Int,

    val independentlyCompletedObjectiveCount: Int,
    val supportedObjectiveCount: Int,
    val directlyAssistedObjectiveCount: Int,

    val strongObjectiveTitles:
    List<String>,

    val improvementObjectiveTitles:
    List<String>,

    val correctlyUsedTargetVocabulary:
    Set<String>,

    val unusedTargetVocabulary:
    Set<String>,

    /*
     * Evidence contribution for each CEFR learning dimension.
     *
     * These are not directly added as percentages.
     * CalculateCefrProgressUseCase combines them using weighted
     * evidence.
     */
    val dimensionScores:
    Map<LearningDimension, Int>
) {

    val objectiveCompletionPercentage: Int
        get() {
            if (totalObjectiveCount <= 0) {
                return 0
            }

            return (
                    completedObjectiveCount
                        .toFloat() /
                            totalObjectiveCount
                                .toFloat() *
                            100f
                    )
                .toInt()
                .coerceIn(
                    minimumValue =
                        0,
                    maximumValue =
                        100
                )
        }

    val requiredObjectiveCompletionPercentage: Int
        get() {
            if (totalRequiredObjectiveCount <= 0) {
                return 100
            }

            return (
                    completedRequiredObjectiveCount
                        .toFloat() /
                            totalRequiredObjectiveCount
                                .toFloat() *
                            100f
                    )
                .toInt()
                .coerceIn(
                    minimumValue =
                        0,
                    maximumValue =
                        100
                )
        }
}