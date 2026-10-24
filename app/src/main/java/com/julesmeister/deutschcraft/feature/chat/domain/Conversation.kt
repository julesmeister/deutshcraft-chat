package com.julesmeister.deutschcraft.feature.chat.domain

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.database.entity.ConversationTitleSource

data class Conversation(
    val id: String,
    val title: String,
    val titleSource: ConversationTitleSource,
    val cefrLevel: CefrLevel,
    val localDate: String,
    val epochDay: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val closedAt: Long?,
    val messageCount: Int,
    val previewText: String?,
    val isClosed: Boolean,
    val isArchived: Boolean
)