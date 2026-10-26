package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion

data class ChatUiState(
    val messages: List<ChatMessage> =
        emptyList(),

    val messageInput: TextFieldValue =
        TextFieldValue(),

    /*
     * Suggested responses remain temporary UI state.
     */
    val suggestions: List<ReplySuggestion> =
        emptyList(),

    /*
     * Current mission information comes from Room.
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
     * A one-time UI event emitted when an objective changes
     * from incomplete to complete during this chat session.
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