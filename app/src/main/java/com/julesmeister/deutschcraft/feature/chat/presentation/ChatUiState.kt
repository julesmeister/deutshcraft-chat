package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.ui.text.input.TextFieldValue
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
     * Temporary generated mission offer.
     *
     * The generated mission is persisted only after the
     * learner accepts it.
     */
    val missionOffer: MissionOpportunity? =
        null,

    val isCheckingMissionOpportunity: Boolean =
        false,

    val isStartingMission: Boolean =
        false,

    /*
     * Active mission information observed from Room.
     */
    val activeMissionId: String? =
        null,

    val activeMissionTitle: String? =
        null,

    /*
     * Short objective title.
     *
     * Example:
     *
     * "Ask about working hours"
     */
    val currentMissionObjective: String? =
        null,

    /*
     * Learner-facing explanation of what to do next.
     *
     * This comes from MissionObjective.description.
     */
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
     *
     * Examples:
     *
     * "Watch the dative article."
     * "Giving reasons is improving."
     * "Present tense mastered."
     *
     * Mission-objective completion has higher display
     * priority than this event.
     */
    val adaptiveLearningFeedbackEvent:
    AdaptiveLearningFeedbackEvent? =
        null,

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