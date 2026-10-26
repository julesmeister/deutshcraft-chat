package com.julesmeister.deutschcraft.feature.mission.domain

data class MissionObjectiveProgress(
    val objective: MissionObjective,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val completedByMessageId: String?,
    val confidence: Float
) {
    val normalizedConfidence: Float
        get() = confidence.coerceIn(
            minimumValue = 0f,
            maximumValue = 1f
        )
}