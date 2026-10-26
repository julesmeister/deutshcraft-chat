package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjectiveProgress

internal fun ChatUiState.initializationSucceeded():
        ChatUiState {
    return copy(
        isInitializing = false,
        errorMessage = null
    )
}

internal fun ChatUiState.initializationFailed(
    message: String
): ChatUiState {
    return copy(
        isInitializing = false,
        isSending = false,
        isAssistantTyping = false,
        isAssessing = false,
        errorMessage = message
    )
}

internal fun ChatUiState.withMessages(
    messages: List<ChatMessage>
): ChatUiState {
    return copy(
        messages = messages
    )
}

internal fun ChatUiState.withInput(
    value: TextFieldValue
): ChatUiState {
    return copy(
        messageInput = value
    )
}

internal fun ChatUiState.sendingStarted():
        ChatUiState {
    return copy(
        messageInput =
            TextFieldValue(),
        suggestions =
            emptyList(),
        isSending =
            true,
        isAssistantTyping =
            true,
        isAssessing =
            true,
        errorMessage =
            null
    )
}

internal fun ChatUiState.sendingSucceeded(
    suggestions: List<ReplySuggestion>,
    warning: String?
): ChatUiState {
    return copy(
        suggestions =
            suggestions,
        isSending =
            false,
        isAssistantTyping =
            false,
        isAssessing =
            false,
        errorMessage =
            warning
    )
}

internal fun ChatUiState.sendingFailed(
    message: String
): ChatUiState {
    return copy(
        isSending = false,
        isAssistantTyping = false,
        isAssessing = false,
        errorMessage = message
    )
}

internal fun ChatUiState.withCurrentStreak(
    streakDays: Int
): ChatUiState {
    return copy(
        currentStreak =
            streakDays.coerceAtLeast(0)
    )
}

internal fun ChatUiState.withActiveMission(
    mission: ConversationMission?,
    newlyCompletedObjective:
    MissionObjectiveProgress?
): ChatUiState {
    if (mission == null) {
        return copy(
            activeMissionId =
                null,
            activeMissionTitle =
                null,
            currentMissionObjective =
                null,
            missionProgressPercentage =
                0
        )
    }

    val completionEvent =
        newlyCompletedObjective?.let { progress ->
            val completionTimestamp =
                progress.completedAt
                    ?: System.currentTimeMillis()

            MissionObjectiveCompletionEvent(
                eventId =
                    "${mission.id}:" +
                            "${progress.objective.id}:" +
                            completionTimestamp,
                objectiveId =
                    progress.objective.id,
                objectiveTitle =
                    progress.objective.title
            )
        }

    return copy(
        activeMissionId =
            mission.id,
        activeMissionTitle =
            mission.template.title,
        currentMissionObjective =
            mission.currentObjective
                ?.objective
                ?.title,
        missionProgressPercentage =
            mission
                .progress
                .percentage
                .coerceIn(
                    minimumValue = 0,
                    maximumValue = 100
                ),
        objectiveCompletionEvent =
            completionEvent
                ?: objectiveCompletionEvent
    )
}

internal fun ChatUiState.objectiveCompletionShown(
    eventId: String
): ChatUiState {
    val activeEvent =
        objectiveCompletionEvent

    if (
        activeEvent == null ||
        activeEvent.eventId != eventId
    ) {
        return this
    }

    return copy(
        objectiveCompletionEvent = null
    )
}

internal fun ChatUiState.errorDismissed():
        ChatUiState {
    return copy(
        errorMessage = null
    )
}