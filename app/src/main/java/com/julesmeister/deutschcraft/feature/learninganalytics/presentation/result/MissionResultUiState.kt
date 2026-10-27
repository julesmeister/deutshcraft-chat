package com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result

import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics

/*
 * Presentation state for the mission-completion result sheet.
 *
 * This model contains only learner-facing information.
 *
 * Room entities and repository details remain outside the UI
 * layer.
 */
data class MissionResultUiState(
    val isVisible: Boolean =
        false,

    val missionId: String? =
        null,

    val missionTitle: String =
        "",

    val cefrLevelLabel: String =
        "",

    val overallScore: Int =
        0,

    val conversationScore: Int =
        0,

    val independenceScore: Int =
        0,

    val grammarScore: Int =
        0,

    val vocabularyScore: Int =
        0,

    val comprehensionScore: Int =
        0,

    val interactionScore: Int =
        0,

    val completedObjectiveCount: Int =
        0,

    val totalObjectiveCount: Int =
        0,

    val hintsUsed: Int =
        0,

    val besserCorrectionCount: Int =
        0,

    val resolvedBesserCorrectionCount: Int =
        0,

    val independentlyCompletedObjectiveCount: Int =
        0,

    val supportedObjectiveCount: Int =
        0,

    val directlyAssistedObjectiveCount: Int =
        0,

    val correctlyUsedVocabularyCount: Int =
        0,

    val unusedVocabularyCount: Int =
        0,

    val strongObjectiveTitles:
    List<String> =
        emptyList(),

    val improvementObjectiveTitles:
    List<String> =
        emptyList()
) {

    val hasMissionResult: Boolean
        get() =
            isVisible &&
                    !missionId.isNullOrBlank()

    val safeOverallScore: Int
        get() =
            overallScore.coercePercent()

    val objectiveProgressLabel: String
        get() =
            "$completedObjectiveCount / " +
                    "$totalObjectiveCount"

    val resolvedCorrectionLabel: String
        get() =
            "$resolvedBesserCorrectionCount of " +
                    "$besserCorrectionCount improved"

    val vocabularyProgressLabel: String
        get() {
            val totalVocabularyCount =
                correctlyUsedVocabularyCount +
                        unusedVocabularyCount

            if (totalVocabularyCount <= 0) {
                return "No target vocabulary"
            }

            return "$correctlyUsedVocabularyCount / " +
                    "$totalVocabularyCount demonstrated"
        }

    val supportSummary: String
        get() =
            when {
                hintsUsed <= 0 -> {
                    "Completed without revealed hints"
                }

                hintsUsed == 1 -> {
                    "1 hint used"
                }

                else -> {
                    "$hintsUsed hints used"
                }
            }

    val performanceMessage: String
        get() =
            when (safeOverallScore) {
                in 90..100 -> {
                    "Excellent mission performance"
                }

                in 80..89 -> {
                    "Strong mission performance"
                }

                in 70..79 -> {
                    "Good progress"
                }

                in 60..69 -> {
                    "Developing well"
                }

                else -> {
                    "Keep practicing"
                }
            }

    companion object {

        fun from(
            missionTitle: String,
            analytics:
            MissionAnalytics
        ): MissionResultUiState {
            return MissionResultUiState(
                isVisible =
                    true,
                missionId =
                    analytics.missionId,
                missionTitle =
                    missionTitle
                        .trim()
                        .ifBlank {
                            DEFAULT_MISSION_TITLE
                        },
                cefrLevelLabel =
                    analytics.cefrLevel.name,
                overallScore =
                    analytics.overallScore.coercePercent(),
                conversationScore =
                    analytics
                        .dimensionScores[
                        com.julesmeister.deutschcraft
                            .feature
                            .learninganalytics
                            .domain
                            .LearningDimension
                            .CONVERSATION
                    ]
                        ?.coercePercent()
                        ?: analytics
                            .overallScore
                            .coercePercent(),
                independenceScore =
                    analytics
                        .independenceScore
                        .coercePercent(),
                grammarScore =
                    analytics
                        .grammarScore
                        .coercePercent(),
                vocabularyScore =
                    analytics
                        .vocabularyScore
                        .coercePercent(),
                comprehensionScore =
                    analytics
                        .comprehensionScore
                        .coercePercent(),
                interactionScore =
                    analytics
                        .interactionScore
                        .coercePercent(),
                completedObjectiveCount =
                    analytics
                        .completedObjectiveCount
                        .coerceAtLeast(
                            minimumValue =
                                0
                        ),
                totalObjectiveCount =
                    analytics
                        .totalObjectiveCount
                        .coerceAtLeast(
                            minimumValue =
                                0
                        ),
                hintsUsed =
                    analytics
                        .hintsUsed
                        .coerceAtLeast(
                            minimumValue =
                                0
                        ),
                besserCorrectionCount =
                    analytics
                        .besserCorrectionCount
                        .coerceAtLeast(
                            minimumValue =
                                0
                        ),
                resolvedBesserCorrectionCount =
                    analytics
                        .resolvedBesserCorrectionCount
                        .coerceIn(
                            minimumValue =
                                0,
                            maximumValue =
                                analytics
                                    .besserCorrectionCount
                                    .coerceAtLeast(
                                        0
                                    )
                        ),
                independentlyCompletedObjectiveCount =
                    analytics
                        .independentlyCompletedObjectiveCount
                        .coerceAtLeast(
                            minimumValue =
                                0
                        ),
                supportedObjectiveCount =
                    analytics
                        .supportedObjectiveCount
                        .coerceAtLeast(
                            minimumValue =
                                0
                        ),
                directlyAssistedObjectiveCount =
                    analytics
                        .directlyAssistedObjectiveCount
                        .coerceAtLeast(
                            minimumValue =
                                0
                        ),
                correctlyUsedVocabularyCount =
                    analytics
                        .correctlyUsedTargetVocabulary
                        .size,
                unusedVocabularyCount =
                    analytics
                        .unusedTargetVocabulary
                        .size,
                strongObjectiveTitles =
                    analytics
                        .strongObjectiveTitles
                        .sanitizedTitles(),
                improvementObjectiveTitles =
                    analytics
                        .improvementObjectiveTitles
                        .sanitizedTitles()
            )
        }

        private const val DEFAULT_MISSION_TITLE =
            "Conversation mission"

        private fun List<String>.sanitizedTitles():
                List<String> {
            return asSequence()
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .distinct()
                .take(
                    MAXIMUM_OBJECTIVE_SUMMARY_COUNT
                )
                .toList()
        }

        private const val MAXIMUM_OBJECTIVE_SUMMARY_COUNT =
            4
    }
}

private fun Int.coercePercent():
        Int {
    return coerceIn(
        minimumValue =
            0,
        maximumValue =
            100
    )
}