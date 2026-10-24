package com.julesmeister.deutschcraft.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId"]),
        Index(
            value = ["conversationId", "sequenceNumber"],
            unique = true
        ),
        Index(value = ["createdAt"]),
        Index(value = ["syncStatus"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,

    val conversationId: String,

    val content: String,

    val sender: MessageSenderEntity,

    val sequenceNumber: Long,

    val createdAt: Long,

    val deliveryStatus: String,

    val syncStatus: SyncStatusEntity,

    val remoteVersion: Long?,

    val lastSyncedAt: Long?
)