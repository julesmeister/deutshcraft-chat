// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/ChatSessionRuntime.kt

package com.julesmeister.deutschcraft.feature.chat.presentation

import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import kotlinx.coroutines.Job

/*
 * Holds non-UI runtime information for the currently opened
 * chat session.
 *
 * Jobs are deliberately separated by responsibility.
 *
 * sendingJob:
 *     Critical conversational path only.
 *
 * suggestionJob:
 *     Progressive presentation of optional reply suggestions.
 *
 * postSendLearningJob:
 *     Supplementary learning processing after Emil has already
 *     replied.
 *
 * This separation allows the learner to type and send another
 * message as soon as Emil's visible reply is ready.
 */
class ChatSessionRuntime {

    var activeConversationId: String? =
        null

    /*
     * Complete Room-backed active mission.
     */
    var activeMission: ConversationMission? =
        null

    /*
     * =======================================================
     * INITIALIZATION
     * =======================================================
     */

    var initializationJob: Job? =
        null

    /*
     * =======================================================
     * CRITICAL MESSAGE SEND
     * =======================================================
     *
     * This should exist only while waiting for the actual
     * learner -> Emil conversational turn.
     *
     * It must NOT remain active while optional suggestions or
     * supplementary learning processing continue.
     */
    var sendingJob: Job? =
        null

    /*
     * =======================================================
     * REPLY SUGGESTIONS
     * =======================================================
     *
     * Suggestions are optional assistance and therefore never
     * block the composer.
     *
     * Starting another conversational turn should cancel the
     * previous suggestion job so old suggestions cannot appear
     * underneath a newer Emil response.
     */
    var suggestionJob: Job? =
        null

    /*
     * =======================================================
     * POST-SEND LEARNING
     * =======================================================
     *
     * Besser generation, adaptive learning, mission analysis,
     * and other supplementary work may continue after Emil's
     * reply is visible.
     *
     * This job deliberately does not participate in isSending.
     */
    var postSendLearningJob: Job? =
        null

    /*
     * =======================================================
     * MISSION
     * =======================================================
     */

    var missionCheckJob: Job? =
        null

    var missionActionJob: Job? =
        null

    /*
     * =======================================================
     * DERIVED RUNTIME STATE
     * =======================================================
     */

    val isInitialized: Boolean
        get() =
            activeConversationId !=
                    null

    val isInitializing: Boolean
        get() =
            initializationJob?.isActive ==
                    true

    /*
     * Only the critical conversational request counts as
     * sending.
     */
    val isSending: Boolean
        get() =
            sendingJob?.isActive ==
                    true

    val isLoadingSuggestions: Boolean
        get() =
            suggestionJob?.isActive ==
                    true

    val isProcessingPostSendLearning: Boolean
        get() =
            postSendLearningJob?.isActive ==
                    true

    val isCheckingMissionOpportunity: Boolean
        get() =
            missionCheckJob?.isActive ==
                    true

    val isPerformingMissionAction: Boolean
        get() =
            missionActionJob?.isActive ==
                    true

    /*
     * =======================================================
     * SUGGESTION JOB
     * =======================================================
     */

    fun cancelSuggestionJob() {
        suggestionJob?.cancel()

        suggestionJob =
            null
    }

    /*
     * =======================================================
     * POST-SEND LEARNING JOB
     * =======================================================
     */

    fun cancelPostSendLearningJob() {
        postSendLearningJob?.cancel()

        postSendLearningJob =
            null
    }

    /*
     * =======================================================
     * MISSION CHECK
     * =======================================================
     */

    fun cancelMissionCheck() {
        missionCheckJob?.cancel()

        missionCheckJob =
            null
    }

    /*
     * =======================================================
     * ALL JOBS
     * =======================================================
     */

    fun cancelAllJobs() {
        initializationJob?.cancel()

        sendingJob?.cancel()

        suggestionJob?.cancel()

        postSendLearningJob?.cancel()

        missionCheckJob?.cancel()

        missionActionJob?.cancel()

        initializationJob =
            null

        sendingJob =
            null

        suggestionJob =
            null

        postSendLearningJob =
            null

        missionCheckJob =
            null

        missionActionJob =
            null
    }

    /*
     * Clears everything tied to the current conversation.
     */
    fun clear() {
        cancelAllJobs()

        activeConversationId =
            null

        activeMission =
            null
    }
}