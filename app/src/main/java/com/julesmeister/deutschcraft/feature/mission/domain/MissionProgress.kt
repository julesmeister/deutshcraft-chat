package com.julesmeister.deutschcraft.feature.mission.domain

data class MissionProgress(
    val completedRequiredObjectives: Int,
    val totalRequiredObjectives: Int,
    val completedOptionalObjectives: Int,
    val totalOptionalObjectives: Int,
    val percentage: Int
) {
    val isComplete: Boolean
        get() =
            totalRequiredObjectives > 0 &&
                    completedRequiredObjectives >=
                    totalRequiredObjectives
}