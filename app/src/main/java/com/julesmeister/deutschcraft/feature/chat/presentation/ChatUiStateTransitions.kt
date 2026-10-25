package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion

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

internal fun ChatUiState.errorDismissed():
        ChatUiState {
    return copy(
        errorMessage = null
    )
}