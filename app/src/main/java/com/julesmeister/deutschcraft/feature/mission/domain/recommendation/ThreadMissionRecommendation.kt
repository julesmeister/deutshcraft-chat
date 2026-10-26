package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

/*
 * Learner-facing recommendation shown in the mission cell at
 * the top of the thread-list screen.
 *
 * This is not an active mission yet. The full mission can be
 * generated after the learner selects this recommendation.
 *
 * scenarioType is the stable identity used to prevent
 * repeated suggestions. Different titles such as:
 *
 * - Bestellung im Café
 * - Morgenkaffee
 * - Kaffee im Café
 *
 * all share CAFE_ORDERING.
 */
data class ThreadMissionRecommendation(
    val scenarioType: MissionScenarioType,
    val title: String,
    val description: String,
    val learnerRole: String,
    val assistantRole: String,
    val cefrLevel: CefrLevel,
    val targetSkills: List<String>,
    val objectiveBlueprints: List<String>
) {

    val objectiveCount: Int
        get() =
            objectiveBlueprints.size

    /*
     * Short skill preview for the mission cell.
     *
     * Example:
     * "Berufserfahrung • Begründungen • Rückfragen"
     */
    val skillPreview: String
        get() =
            targetSkills
                .take(
                    MAXIMUM_PREVIEW_SKILL_COUNT
                )
                .joinToString(
                    separator =
                        " • "
                )

    val isUsable: Boolean
        get() =
            title.isNotBlank() &&
                    description.isNotBlank() &&
                    learnerRole.isNotBlank() &&
                    assistantRole.isNotBlank() &&
                    objectiveBlueprints.size in
                    MINIMUM_OBJECTIVE_COUNT..
                    MAXIMUM_OBJECTIVE_COUNT

    companion object {
        const val MINIMUM_OBJECTIVE_COUNT =
            8

        const val MAXIMUM_OBJECTIVE_COUNT =
            10

        private const val MAXIMUM_PREVIEW_SKILL_COUNT =
            3
    }
}

/*
 * Converts a curriculum scenario into the recommendation
 * displayed by the thread list.
 */
fun MissionScenarioDefinition.toThreadRecommendation(
    cefrLevel: CefrLevel
): ThreadMissionRecommendation {
    return ThreadMissionRecommendation(
        scenarioType =
            type,
        title =
            title.trim(),
        description =
            description.trim(),
        learnerRole =
            learnerRole.trim(),
        assistantRole =
            assistantRole.trim(),
        cefrLevel =
            cefrLevel,
        targetSkills =
            targetSkills
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct(),
        objectiveBlueprints =
            objectiveBlueprints
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
                .take(
                    ThreadMissionRecommendation
                        .MAXIMUM_OBJECTIVE_COUNT
                )
    )
}