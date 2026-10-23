package com.julesmeister.deutschcraft.feature.chat.presentation

import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val suggestions: List<ReplySuggestion> = emptyList(),
    val messageInput: String = "",
    val isAssistantTyping: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null
)