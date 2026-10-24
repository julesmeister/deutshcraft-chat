package com.julesmeister.deutschcraft.feature.chat.presentation

import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),

    val suggestions: List<ReplySuggestion> = emptyList(),

    val messageInput: String = "",

    val isInitializing: Boolean = true,

    val isSending: Boolean = false,

    val isAssistantTyping: Boolean = false,

    val isAssessing: Boolean = false,

    val errorMessage: String? = null
) {
    /**
     * The send button is enabled only when:
     *
     * 1. The conversation has finished initializing.
     * 2. The input contains visible characters.
     * 3. Another message is not already being sent.
     */
    val canSend: Boolean
        get() {
            return !isInitializing &&
                    messageInput.isNotBlank() &&
                    !isSending
        }

    /**
     * Useful for deciding whether the empty-chat
     * placeholder should be displayed.
     */
    val isConversationEmpty: Boolean
        get() {
            return messages.isEmpty()
        }

    /**
     * Indicates that some background chat-related
     * activity is currently taking place.
     */
    val isBusy: Boolean
        get() {
            return isInitializing ||
                    isSending ||
                    isAssistantTyping ||
                    isAssessing
        }
}