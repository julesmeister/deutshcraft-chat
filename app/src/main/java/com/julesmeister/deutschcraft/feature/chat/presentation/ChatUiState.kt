package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.besser.presentation.BesserChallengeUiModel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity

data class ChatUiState(
    val messages: List<ChatMessage> =
        emptyList(),

    val messageInput: TextFieldValue =
        TextFieldValue(),

    val suggestions: List<ReplySuggestion> =
        emptyList(),

    /*
     * =======================================================
     * Besser
     * =======================================================
     *
     * Active Besser challenges keyed by their owning learner
     * message ID.
     *
     * This deliberately mirrors the marked-phrase attachment
     * approach:
     *
     *     message.id -> Besser challenge
     *
     * ChatMessageList can therefore attach the challenge
     * directly underneath the correct learner bubble.
     */
    val besserChallenges:
    Map<String, BesserChallengeUiModel> =
        emptyMap(),

    /*
     * Temporary generated mission offer.
     */
    val missionOffer: MissionOpportunity? =
        null,

    val isCheckingMissionOpportunity: Boolean =
        false,

    val isStartingMission: Boolean =
        false,

    /*
     * =======================================================
     * Active mission
     * =======================================================
     */

    val activeMissionId: String? =
        null,

    val activeMissionTitle: String? =
        null,

    val currentMissionObjective: String? =
        null,

    val currentMissionGuidance: String? =
        null,

    val missionProgressPercentage: Int =
        0,

    /*
     * One-time mission-objective completion feedback.
     */
    val objectiveCompletionEvent:
    MissionObjectiveCompletionEvent? =
        null,

    /*
     * One-time adaptive-learning feedback.
     */
    val adaptiveLearningFeedbackEvent:
    AdaptiveLearningFeedbackEvent? =
        null,

    /*
     * =======================================================
     * Runtime presentation state
     * =======================================================
     */

    val isInitializing: Boolean =
        true,

    val isSending: Boolean =
        false,

    val isAssistantTyping: Boolean =
        false,

    val isAssessing: Boolean =
        false,

    val errorMessage: String? =
        null,

    val currentStreak: Int =
        0
)