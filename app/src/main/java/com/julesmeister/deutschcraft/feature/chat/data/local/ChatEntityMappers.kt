package com.julesmeister.deutschcraft.feature.chat.data.local

import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity
import com.julesmeister.deutschcraft.core.database.entity.MessageEntity
import com.julesmeister.deutschcraft.core.database.entity.MessageSenderEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender

fun ConversationEntity.toDomain(): Conversation {
    return Conversation(
        id = id,
        title = title,
        currentCefrLevel = currentCefrLevel,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastMessagePreview = lastMessagePreview,
        isArchived = isArchived
    )
}

fun Conversation.toEntity(): ConversationEntity {
    return ConversationEntity(
        id = id,
        title = title,
        currentCefrLevel = currentCefrLevel,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastMessagePreview = lastMessagePreview,
        isArchived = isArchived,
        syncStatus = SyncStatusEntity.LOCAL_ONLY,
        remoteVersion = null,
        lastSyncedAt = null
    )
}

fun MessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        id = id,
        conversationId = conversationId,
        content = content,
        sender = sender.toDomain(),
        sequenceNumber = sequenceNumber,
        timestamp = createdAt
    )
}

fun ChatMessage.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        conversationId = conversationId,
        content = content,
        sender = sender.toEntity(),
        sequenceNumber = sequenceNumber,
        createdAt = timestamp,
        deliveryStatus = "SENT",
        syncStatus = SyncStatusEntity.LOCAL_ONLY,
        remoteVersion = null,
        lastSyncedAt = null
    )
}

private fun MessageSenderEntity.toDomain(): MessageSender {
    return when (this) {
        MessageSenderEntity.USER ->
            MessageSender.USER

        MessageSenderEntity.ASSISTANT,
        MessageSenderEntity.SYSTEM ->
            MessageSender.ASSISTANT
    }
}

private fun MessageSender.toEntity(): MessageSenderEntity {
    return when (this) {
        MessageSender.USER ->
            MessageSenderEntity.USER

        MessageSender.ASSISTANT ->
            MessageSenderEntity.ASSISTANT
    }
}