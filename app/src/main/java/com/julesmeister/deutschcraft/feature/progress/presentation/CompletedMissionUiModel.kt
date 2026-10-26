package com.julesmeister.deutschcraft.feature.progress.presentation

data class CompletedMissionUiModel(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val completedAt: Long,
    val completedObjectiveCount: Int,
    val totalObjectiveCount: Int
) {

    val safeIcon: String
        get() =
            icon
                .trim()
                .takeIf(String::isNotBlank)
                ?: DEFAULT_MISSION_ICON

    val completionLabel: String
        get() =
            "$completedObjectiveCount von " +
                    "$totalObjectiveCount Zielen"

    private companion object {
        const val DEFAULT_MISSION_ICON =
            "🎯"
    }
}