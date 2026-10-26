package com.julesmeister.deutschcraft.feature.progress.presentation

import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSummary

data class ProgressUiState(
    val progress: ProgressSummary? =
        null,

    val completedMissions:
    List<CompletedMissionUiModel> =
        emptyList(),

    val isProgressLoading: Boolean =
        true,

    val areMissionsLoading: Boolean =
        true,

    val progressErrorMessage: String? =
        null,

    val missionErrorMessage: String? =
        null
) {

    val completedMissionCount: Int
        get() =
            completedMissions.size

    val recentCompletedMissions:
            List<CompletedMissionUiModel>
        get() =
            completedMissions.take(
                RECENT_MISSION_LIMIT
            )

    /*
     * Keep the page loading until both independent data
     * sources have emitted their first result.
     */
    val isLoading: Boolean
        get() =
            isProgressLoading ||
                    areMissionsLoading

    val errorMessage: String?
        get() =
            progressErrorMessage
                ?: missionErrorMessage

    val hasProgressData: Boolean
        get() =
            progress != null

    val hasCompletedMissions: Boolean
        get() =
            completedMissions.isNotEmpty()

    val hasAnyData: Boolean
        get() =
            hasProgressData ||
                    hasCompletedMissions

    private companion object {
        const val RECENT_MISSION_LIMIT =
            5
    }
}