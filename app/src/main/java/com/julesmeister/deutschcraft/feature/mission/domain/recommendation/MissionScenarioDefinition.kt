package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

/*
 * Curriculum definition for one possible mission scenario.
 *
 * This is not yet a generated MissionTemplate.
 *
 * It tells the mission generator:
 *
 * - Which scenario to generate
 * - Which CEFR levels it supports
 * - Which roles should be used
 * - Which communication skills should be reinforced
 * - Which objective progression should be represented
 */
data class MissionScenarioDefinition(
    val type: MissionScenarioType,

    /*
     * Stable learner-facing German title.
     *
     * Qwen may refine the title, but the scenario identity
     * remains unchanged.
     */
    val title: String,

    val description: String,

    val learnerRole: String,

    val assistantRole: String,

    val minimumLevel: CefrLevel,

    val maximumLevel: CefrLevel,

    /*
     * Skills that make the scenario educational.
     *
     * These are prompt labels rather than enum dependencies,
     * keeping the mission curriculum independent from the
     * adaptive-learning feature package.
     */
    val targetSkills: Set<String>,

    /*
     * Suggested ten-step progression.
     *
     * The mission generator can adapt these steps to existing
     * conversation context, but should preserve the learning
     * progression.
     */
    val objectiveBlueprints: List<String>,

    /*
     * Keywords used only as a weak relevance signal.
     *
     * Recent conversation vocabulary must not override CEFR
     * balance or diversity.
     */
    val topicKeywords: Set<String> =
        emptySet()
) {

    val targetObjectiveCount: Int
        get() =
            objectiveBlueprints
                .size
                .coerceIn(
                    minimumValue =
                        MINIMUM_OBJECTIVE_COUNT,
                    maximumValue =
                        MAXIMUM_OBJECTIVE_COUNT
                )

    fun supports(
        level: CefrLevel
    ): Boolean {
        return level.rank in
                minimumLevel.rank..maximumLevel.rank
    }

    private val CefrLevel.rank: Int
        get() =
            when (this) {
                CefrLevel.A1 -> 1
                CefrLevel.A2 -> 2
                CefrLevel.B1 -> 3
                CefrLevel.B2 -> 4
                CefrLevel.C1 -> 5
                CefrLevel.C2 -> 6
            }

    private companion object {
        const val MINIMUM_OBJECTIVE_COUNT =
            8

        const val MAXIMUM_OBJECTIVE_COUNT =
            10
    }
}