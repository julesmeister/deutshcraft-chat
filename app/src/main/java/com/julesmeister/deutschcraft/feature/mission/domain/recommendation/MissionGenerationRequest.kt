package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

data class MissionGenerationRequest(
    val cefrLevel: CefrLevel,

    val selectedScenario:
    MissionScenarioDefinition,

    /*
     * Recent context helps Qwen continue naturally.
     *
     * It cannot change the selected scenario type.
     */
    val recentConversationText: String,

    /*
     * Objective blueprints already demonstrated in the
     * current conversation may be marked completed or
     * replaced with a later equivalent step.
     */
    val alreadyDemonstratedBehaviors:
    List<String> =
        emptyList()
)