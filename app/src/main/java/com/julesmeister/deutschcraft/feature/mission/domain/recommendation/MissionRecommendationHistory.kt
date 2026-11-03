// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/recommendation/MissionRecommendationHistory.kt

package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

data class MissionRecommendationHistory(
    val scenarioTypes:
    List<MissionScenarioType> =
        emptyList(),

    val completedScenarioTypes:
    List<MissionScenarioType> =
        emptyList(),

    val dismissedScenarioTypes:
    List<MissionScenarioType> =
        emptyList(),

    val blockedScenarioTypes: Set<MissionScenarioType> = emptySet(),
    val recentConversationText: String = ""
) {

    val contextCategories:
            List<MissionScenarioContextCategory>
        get() =
            scenarioTypes.map(
                MissionScenarioType::contextCategory
            )

    val completedContextCategories:
            List<MissionScenarioContextCategory>
        get() =
            completedScenarioTypes.map(
                MissionScenarioType::contextCategory
            )

    val dismissedContextCategories:
            List<MissionScenarioContextCategory>
        get() =
            dismissedScenarioTypes.map(
                MissionScenarioType::contextCategory
            )

    companion object {

        val Empty =
            MissionRecommendationHistory()
    }
}