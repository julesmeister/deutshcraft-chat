package com.julesmeister.deutschcraft.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["updatedAt"]),
        Index(value = ["syncStatus"])
    ]
)
data class ConversationEntity(
    @PrimaryKey
    val id: String,

    val title: String,

    val currentCefrLevel: String,

    val createdAt: Long,

    val updatedAt: Long,

    val lastMessagePreview: String?,

    val isArchived: Boolean,

    val syncStatus: SyncStatusEntity,

    val remoteVersion: Long?,

    val lastSyncedAt: Long?
)