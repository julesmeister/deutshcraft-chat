package com.julesmeister.deutschcraft.feature.chat.domain

data class ChatMessage(
    val id: String,
    val content: String,
    val sender: MessageSender,
    val timestamp: Long
)

enum class MessageSender {
    USER,
    ASSISTANT
}