package com.julesmeister.deutschcraft.feature.chat.presentation

import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import kotlinx.coroutines.Job

/*
 * Holds non-UI runtime information for the currently opened
 * chat session.
 *
 * These values do not belong in ChatUiState because they are
 * used to coordinate background work rather than directly
 * render UI.
 */
class ChatSessionRuntime {

    var activeConversationId: String? =
        null

    /*
     * Complete Room-backed mission domain model.
     *
     * ChatMessageSender needs this to evaluate a learner
     * message against the unfinished mission objectives.
     */
    var activeMission: ConversationMission? =
        null

    var initializationJob: Job? =
        null

    var sendingJob: Job? =
        null

    var missionCheckJob: Job? =
        null

    var missionActionJob: Job? =
        null

    val isInitialized: Boolean
        get() =
            activeConversationId != null

    val isInitializing: Boolean
        get() =
            initializationJob?.isActive == true

    val isSending: Boolean
        get() =
            sendingJob?.isActive == true

    val isCheckingMissionOpportunity: Boolean
        get() =
            missionCheckJob?.isActive == true

    val isPerformingMissionAction: Boolean
        get() =
            missionActionJob?.isActive == true

    fun cancelMissionCheck() {
        missionCheckJob?.cancel()

        missionCheckJob =
            null
    }

    fun cancelAllJobs() {
        initializationJob?.cancel()
        sendingJob?.cancel()
        missionCheckJob?.cancel()
        missionActionJob?.cancel()

        initializationJob =
            null

        sendingJob =
            null

        missionCheckJob =
            null

        missionActionJob =
            null
    }

    /*
     * Clears everything tied to the current conversation.
     *
     * This prevents a mission from one conversation from
     * being reused accidentally in another conversation.
     */
    fun clear() {
        cancelAllJobs()

        activeConversationId =
            null

        activeMission =
            null
    }
}