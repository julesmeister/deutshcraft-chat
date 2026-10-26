package com.julesmeister.deutschcraft.feature.mission.domain

/*
 * Mission information needed while generating Emil's next
 * response.
 *
 * This model deliberately contains only conversation-facing
 * information. Room entities and objective-progress records
 * are not exposed to the chat AI gateway.
 */
data class ActiveMissionConversationContext(
    val missionId: String,
    val title: String,
    val scenario: String,
    val learnerRole: String,
    val assistantRole: String,
    val progressPercentage: Int,
    val currentObjective:
    MissionConversationObjective,
    val remainingObjectives:
    List<MissionConversationObjective>
) {

    val safeProgressPercentage: Int
        get() =
            progressPercentage.coerceIn(
                minimumValue = 0,
                maximumValue = 100
            )
}

data class MissionConversationObjective(
    val id: String,
    val title: String,
    val description: String,
    val coachingHints: List<String> =
        emptyList(),
    val examplePhrases: List<String> =
        emptyList(),
    val targetVocabulary: List<String> =
        emptyList()
)