package com.julesmeister.deutschcraft.feature.chat.domain

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val content: String,
    val sender: MessageSender,
    val sequenceNumber: Long,
    val timestamp: Long,
    var correction: String? = null
)

enum class MessageSender {
    USER,
    ASSISTANT
}