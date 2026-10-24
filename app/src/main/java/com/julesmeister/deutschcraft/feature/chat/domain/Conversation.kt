package com.julesmeister.deutschcraft.feature.chat.domain

data class Conversation(
    val id: String,
    val title: String,
    val currentCefrLevel: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastMessagePreview: String?,
    val isArchived: Boolean
)