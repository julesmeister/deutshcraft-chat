package com.julesmeister.deutschcraft.feature.learninganalytics.domain

/*
 * Small mission result model suitable for a completion
 * dialog, sheet, or mission top-bar transition.
 */
data class MissionAnalyticsSummary(
    val missionId: String,
    val overallScore: Int,
    val independenceScore: Int,
    val grammarScore: Int,
    val vocabularyScore: Int,
    val comprehensionScore: Int,
    val interactionScore: Int,
    val completedObjectiveCount: Int,
    val totalObjectiveCount: Int,
    val hintsUsed: Int,
    val strongObjectiveTitles: List<String>,
    val improvementObjectiveTitles: List<String>
) {

    val safeOverallScore: Int
        get() =
            overallScore.coercePercent()

    val safeIndependenceScore: Int
        get() =
            independenceScore.coercePercent()

    val objectiveProgressLabel: String
        get() =
            "$completedObjectiveCount / " +
                    "$totalObjectiveCount"

    private fun Int.coercePercent():
            Int {
        return coerceIn(
            minimumValue =
                0,
            maximumValue =
                100
        )
    }
}

fun MissionAnalytics.toSummary():
        MissionAnalyticsSummary {
    return MissionAnalyticsSummary(
        missionId =
            missionId,
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
        completedObjectiveCount =
            completedObjectiveCount,
        totalObjectiveCount =
            totalObjectiveCount,
        hintsUsed =
            hintsUsed,
        strongObjectiveTitles =
            strongObjectiveTitles,
        improvementObjectiveTitles =
            improvementObjectiveTitles
    )
}