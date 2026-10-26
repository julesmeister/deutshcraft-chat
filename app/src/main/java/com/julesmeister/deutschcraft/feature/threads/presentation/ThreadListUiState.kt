package com.julesmeister.deutschcraft.feature.threads.presentation

import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import com.julesmeister.deutschcraft.feature.threads.domain.ThreadSection

data class ThreadListUiState(
    val sections: List<ThreadSection> =
        emptyList(),

    val archivedThreads:
    List<ConversationThread> =
        emptyList(),

    /*
     * One AI-generated conversation waiting to be opened.
     *
     * It is rendered like a normal thread with a lime
     * background.
     */
    val missionRecommendation:
    MissionThreadRecommendation? =
        null,

    val isLoading: Boolean =
        true,

    val isLoadingMissionRecommendation: Boolean =
        false,

    val isCreatingThread: Boolean =
        false,

    val isStartingRecommendedMission: Boolean =
        false,

    val errorMessage: String? =
        null
) {

    val isEmpty: Boolean
        get() {
            return !isLoading &&
                    sections.isEmpty() &&
                    missionRecommendation == null
        }

    val archivedThreadCount: Int
        get() =
            archivedThreads.size

    val hasArchivedThreads: Boolean
        get() =
            archivedThreads.isNotEmpty()

    val totalThreadCount: Int
        get() {
            return sections.sumOf { section ->
                section.threads.size
            }
        }
}