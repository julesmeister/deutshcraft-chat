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

    /*
     * Global learning streak displayed in the thread-list
     * top bar.
     *
     * This information belongs on the home/dashboard screen
     * rather than inside an individual conversation.
     */
    val currentStreak: Int =
        0,

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

    /*
     * The screen is considered empty only when:
     *
     * - Thread loading has completed
     * - There are no saved active threads
     * - There is no generated mission recommendation
     *
     * Archived conversations are handled separately through
     * hasArchivedThreads.
     */
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