package com.julesmeister.deutschcraft.feature.mission.domain

data class MissionObjective(
    val id: String,
    val missionTemplateId: String,
    val title: String,
    val description: String,
    val sequenceNumber: Int,
    val isRequired: Boolean,
    val completionHint: String
)