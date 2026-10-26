package com.julesmeister.deutschcraft.feature.mission.domain.generated

import com.julesmeister.deutschcraft.feature.mission.domain.MissionDifficulty

data class GeneratedMissionDraft(
    val title: String,
    val description: String,
    val scenario: String,
    val learnerRole: String,
    val assistantRole: String,
    val difficulty: MissionDifficulty,
    val icon: String,
    val objectives:
    List<GeneratedMissionObjectiveDraft>
)