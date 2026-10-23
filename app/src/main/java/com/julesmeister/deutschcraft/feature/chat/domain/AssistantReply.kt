package com.julesmeister.deutschcraft.feature.chat.domain

data class AssistantReply(
    val message: ChatMessage,
    val suggestions: List<ReplySuggestion>
)