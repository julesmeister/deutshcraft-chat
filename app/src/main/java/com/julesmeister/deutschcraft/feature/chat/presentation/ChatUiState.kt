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

    /*
     * Suggested responses remain temporary UI state and are
     * restored from the in-memory conversation cache.
     */
    val suggestions: List<ReplySuggestion> =
        emptyList(),

    /*
     * Generated mission offers remain temporary until the
     * learner accepts one.
     *
     * Accepted missions are persisted through the mission
     * repository.
     */
    val missionOffer: MissionOpportunity? =
        null,

    val isCheckingMissionOpportunity: Boolean =
        false,

    val isStartingMission: Boolean =
        false,

    /*
     * Active mission information comes from Room.
     */
    val activeMissionId: String? =
        null,

    val activeMissionTitle: String? =
        null,

    val currentMissionObjective: String? =
        null,

    val missionProgressPercentage: Int =
        0,

    /*
     * One-time UI event emitted when an objective changes
     * from incomplete to complete during this session.
     */
    val objectiveCompletionEvent:
    MissionObjectiveCompletionEvent? =
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