package com.julesmeister.deutschcraft.feature.progress.domain.model

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
data class ProgressSummary(
    val currentLevel: CefrLevel,
    val readinessPercentage: Int,

    val grammarScore: Int,
    val vocabularyScore: Int,
    val coherenceScore: Int,
    val accuracyScore: Int,
    val rangeScore: Int,

    val overallScore: Int,
    val assessedMessageCount: Int,
    val activeDayCount: Int
) {

    val normalizedReadiness: Float
        get() = readinessPercentage
            .coerceIn(
                minimumValue = MINIMUM_SCORE,
                maximumValue = MAXIMUM_SCORE
            ) / MAXIMUM_SCORE.toFloat()

    val normalizedOverallScore: Float
        get() = overallScore
            .coerceIn(
                minimumValue = MINIMUM_SCORE,
                maximumValue = MAXIMUM_SCORE
            ) / MAXIMUM_SCORE.toFloat()

    val nextLevelName: String
        get() = when (currentLevel.name.uppercase()) {
            "A1" -> "A2"
            "A2" -> "B1"
            "B1" -> "B2"
            "B2" -> "C1"
            "C1" -> "C2"
            "C2" -> "C2"
            else -> "A1"
        }

    val isHighestLevel: Boolean
        get() = currentLevel.name.equals(
            other = "C2",
            ignoreCase = true
        )

    val confidence: ProgressConfidence
        get() = when {
            assessedMessageCount < EARLY_EVIDENCE_THRESHOLD -> {
                ProgressConfidence.NOT_ENOUGH_DATA
            }

            assessedMessageCount <
                    DEVELOPING_EVIDENCE_THRESHOLD -> {
                ProgressConfidence.EARLY
            }

            assessedMessageCount <
                    RELIABLE_EVIDENCE_THRESHOLD -> {
                ProgressConfidence.DEVELOPING
            }

            else -> {
                ProgressConfidence.RELIABLE
            }
        }

    val skillSummaries: List<ProgressSkillSummary>
        get() = listOf(
            ProgressSkillSummary(
                label = "Grammatik",
                score = grammarScore
            ),
            ProgressSkillSummary(
                label = "Wortschatz",
                score = vocabularyScore
            ),
            ProgressSkillSummary(
                label = "Kohärenz",
                score = coherenceScore
            ),
            ProgressSkillSummary(
                label = "Genauigkeit",
                score = accuracyScore
            ),
            ProgressSkillSummary(
                label = "Sprachumfang",
                score = rangeScore
            )
        )

    private companion object {
        const val MINIMUM_SCORE = 0
        const val MAXIMUM_SCORE = 100

        const val EARLY_EVIDENCE_THRESHOLD = 5
        const val DEVELOPING_EVIDENCE_THRESHOLD = 15
        const val RELIABLE_EVIDENCE_THRESHOLD = 40
    }
}