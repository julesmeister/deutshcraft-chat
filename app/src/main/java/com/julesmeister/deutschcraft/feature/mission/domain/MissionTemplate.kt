package com.julesmeister.deutschcraft.feature.mission.domain

data class MissionTemplate(
    val id: String,
    val title: String,
    val description: String,
    val scenario: String,
    val learnerRole: String,
    val assistantRole: String,
    val difficulty: MissionDifficulty,
    val icon: String,
    val objectives: List<MissionObjective>,
    val isEnabled: Boolean
)