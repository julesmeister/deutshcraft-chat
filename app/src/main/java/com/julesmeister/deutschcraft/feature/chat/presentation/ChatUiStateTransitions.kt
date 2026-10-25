package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageCorrection
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

internal fun ChatUiState.withMessagesAndCorrections(
    messages: List<ChatMessage>,
    savedCorrections: Map<String, String>
): ChatUiState {
    val mergedCorrections =
        savedCorrections + corrections

    return copy(
        messages = messages,
        corrections = mergedCorrections
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
        messageInput = TextFieldValue(),
        suggestions = emptyList(),
        isSending = true,
        isAssistantTyping = true,
        isAssessing = true,
        errorMessage = null
    )
}

internal fun ChatUiState.sendingSucceeded(
    suggestions: List<ReplySuggestion>,
    correction: MessageCorrection?,
    warning: String?
): ChatUiState {
    val updatedCorrections =
        if (correction == null) {
            corrections
        } else {
            corrections + mapOf(
                correction.messageId to
                        correction.correctedSentence
            )
        }

    return copy(
        suggestions = suggestions,
        corrections = updatedCorrections,
        isSending = false,
        isAssistantTyping = false,
        isAssessing = false,
        errorMessage = warning
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