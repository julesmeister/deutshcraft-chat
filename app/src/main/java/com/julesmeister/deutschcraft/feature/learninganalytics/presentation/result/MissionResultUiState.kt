// app/src/main/java/com/julesmeister/deutschcraft/feature/learninganalytics/presentation/result/MissionResultUiState.kt

package com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result

import com.julesmeister.deutschcraft.feature.learninganalytics.domain.LearningDimension
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics

data class MissionResultUiState(
    val isVisible: Boolean =
        false,

    val missionId: String? =
        null,

    val missionTitle: String =
        "",

    val cefrLevelLabel: String =
        "",

    /*
     * Numeric values remain available to the presentation
     * layer, but the result sheet converts them into
     * learner-friendly growth labels instead of presenting the
     * mission as an exam score.
     */
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

    val completedRequiredObjectiveCount: Int =
        0,

    val totalRequiredObjectiveCount: Int =
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

    val strongObjectiveTitles:
    List<String> =
        emptyList(),

    val improvementObjectiveTitles:
    List<String> =
        emptyList(),

    val correctlyUsedTargetVocabulary:
    List<String> =
        emptyList(),

    val unusedTargetVocabulary:
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
        get() {
            if (totalObjectiveCount <= 0) {
                return "No objectives recorded"
            }

            return "$completedObjectiveCount of " +
                    "$totalObjectiveCount completed"
        }

    val requiredObjectiveProgressLabel: String
        get() {
            if (totalRequiredObjectiveCount <= 0) {
                return "No required objectives"
            }

            return "$completedRequiredObjectiveCount of " +
                    "$totalRequiredObjectiveCount required objectives"
        }

    val missionCompletionLabel: String
        get() =
            when {
                totalObjectiveCount <= 0 ->
                    "Conversation completed"

                completedObjectiveCount >=
                        totalObjectiveCount ->
                    "All conversation goals completed"

                completedRequiredObjectiveCount >=
                        totalRequiredObjectiveCount &&
                        totalRequiredObjectiveCount > 0 ->
                    "Required conversation goals completed"

                else ->
                    objectiveProgressLabel
            }

    val overallGrowthLabel: String
        get() =
            safeOverallScore.toGrowthLabel()

    val conversationGrowthLabel: String
        get() =
            conversationScore.toGrowthLabel()

    val independenceGrowthLabel: String
        get() =
            independenceScore.toGrowthLabel()

    val grammarGrowthLabel: String
        get() =
            grammarScore.toGrowthLabel()

    val vocabularyGrowthLabel: String
        get() =
            vocabularyScore.toGrowthLabel()

    val comprehensionGrowthLabel: String
        get() =
            comprehensionScore.toGrowthLabel()

    val interactionGrowthLabel: String
        get() =
            interactionScore.toGrowthLabel()

    val independenceSummary: String
        get() =
            when {
                independentlyCompletedObjectiveCount <= 0 &&
                        supportedObjectiveCount <= 0 &&
                        directlyAssistedObjectiveCount <= 0 ->
                    "No support evidence recorded"

                directlyAssistedObjectiveCount == 0 &&
                        supportedObjectiveCount == 0 ->
                    "Completed independently"

                independentlyCompletedObjectiveCount >
                        supportedObjectiveCount +
                        directlyAssistedObjectiveCount ->
                    "Mostly independent"

                directlyAssistedObjectiveCount == 0 ->
                    "Light support used"

                else ->
                    "Support helped move the conversation forward"
            }

    val hintSummary: String
        get() =
            when (hintsUsed) {
                0 ->
                    "No revealed hints"

                1 ->
                    "1 hint used"

                else ->
                    "$hintsUsed hints used"
            }

    val besserSummary: String?
        get() {
            if (besserCorrectionCount <= 0) {
                return null
            }

            if (resolvedBesserCorrectionCount <= 0) {
                return "$besserCorrectionCount Besser " +
                        correctionWord(
                            count =
                                besserCorrectionCount
                        )
            }

            return "$resolvedBesserCorrectionCount of " +
                    "$besserCorrectionCount Besser " +
                    correctionWord(
                        count =
                            besserCorrectionCount
                    ) +
                    " improved"
        }

    val supportSummary: String
        get() =
            buildList {
                add(
                    independenceSummary
                )

                add(
                    hintSummary
                )

                besserSummary?.let(
                    ::add
                )
            }
                .joinToString(
                    separator =
                        " • "
                )

    val hasStrongObjectives: Boolean
        get() =
            strongObjectiveTitles.isNotEmpty()

    val hasImprovementObjectives: Boolean
        get() =
            improvementObjectiveTitles.isNotEmpty()

    val hasDemonstratedVocabulary: Boolean
        get() =
            correctlyUsedTargetVocabulary.isNotEmpty()

    val hasUnusedVocabulary: Boolean
        get() =
            unusedTargetVocabulary.isNotEmpty()

    val demonstratedVocabularyLabel: String
        get() =
            when (
                correctlyUsedTargetVocabulary.size
            ) {
                0 ->
                    "No target vocabulary demonstrated yet"

                1 ->
                    "1 target expression demonstrated"

                else ->
                    "${correctlyUsedTargetVocabulary.size} " +
                            "target expressions demonstrated"
            }

    val nextFocusMessage: String
        get() =
            when {
                improvementObjectiveTitles.isNotEmpty() ->
                    "DeutschCraft will keep reinforcing these " +
                            "conversation skills in future missions."

                unusedTargetVocabulary.isNotEmpty() ->
                    "The conversation went well. Future missions " +
                            "will create more opportunities to use the " +
                            "remaining target vocabulary."

                safeOverallScore >=
                        STRONG_SCORE_THRESHOLD ->
                    "You handled this conversation strongly. " +
                            "The next missions can build on these skills " +
                            "in a different situation."

                else ->
                    "DeutschCraft will keep practising these skills " +
                            "through new conversation contexts."
            }

    companion object {

        fun from(
            missionTitle:
            String,

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
                    analytics
                        .overallScore
                        .coercePercent(),

                conversationScore =
                    analytics
                        .dimensionScores[
                        LearningDimension.CONVERSATION
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

                completedRequiredObjectiveCount =
                    analytics
                        .completedRequiredObjectiveCount
                        .coerceAtLeast(
                            minimumValue =
                                0
                        ),

                totalRequiredObjectiveCount =
                    analytics
                        .totalRequiredObjectiveCount
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
                                        minimumValue =
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

                strongObjectiveTitles =
                    analytics
                        .strongObjectiveTitles
                        .sanitizedValues(
                            maximumCount =
                                MAXIMUM_OBJECTIVE_SUMMARY_COUNT
                        ),

                improvementObjectiveTitles =
                    analytics
                        .improvementObjectiveTitles
                        .sanitizedValues(
                            maximumCount =
                                MAXIMUM_OBJECTIVE_SUMMARY_COUNT
                        ),

                correctlyUsedTargetVocabulary =
                    analytics
                        .correctlyUsedTargetVocabulary
                        .toList()
                        .sanitizedValues(
                            maximumCount =
                                MAXIMUM_VOCABULARY_SUMMARY_COUNT
                        ),

                unusedTargetVocabulary =
                    analytics
                        .unusedTargetVocabulary
                        .toList()
                        .sanitizedValues(
                            maximumCount =
                                MAXIMUM_VOCABULARY_SUMMARY_COUNT
                        )
            )
        }

        private fun List<String>.sanitizedValues(
            maximumCount:
            Int
        ): List<String> {

            return asSequence()
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .distinct()
                .take(
                    maximumCount
                )
                .toList()
        }

        private const val DEFAULT_MISSION_TITLE =
            "Conversation mission"

        private const val MAXIMUM_OBJECTIVE_SUMMARY_COUNT =
            4

        private const val MAXIMUM_VOCABULARY_SUMMARY_COUNT =
            8

        private const val STRONG_SCORE_THRESHOLD =
            75
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

private fun Int.toGrowthLabel():
        String {

    return when (
        coercePercent()
    ) {
        in 85..100 ->
            "Strong"

        in 70..84 ->
            "Improving"

        in 50..69 ->
            "Practising"

        else ->
            "Developing"
    }
}

private fun correctionWord(
    count:
    Int
): String {

    return if (count == 1) {
        "correction"
    } else {
        "corrections"
    }
}