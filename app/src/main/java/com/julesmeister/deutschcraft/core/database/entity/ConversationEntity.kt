// app/src/main/java/com/julesmeister/deutschcraft/core/database/entity/ConversationEntity.kt

package com.julesmeister.deutschcraft.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["epochDay"]),
        Index(value = ["updatedAt"]),
        Index(value = ["isClosed"]),
        Index(value = ["isArchived"]),
        Index(value = ["isDeleted"]),
        Index(value = ["syncStatus"])
    ]
)
data class ConversationEntity(
    @PrimaryKey
    val id: String,

    val title: String,

    val titleSource:
    ConversationTitleSource,

    val cefrLevel:
    CefrLevel,

    val localDate: String,

    val epochDay: Long,

    val createdAt: Long,

    val updatedAt: Long,

    val closedAt: Long?,

    val messageCount: Int,

    val previewText: String?,

    val isClosed: Boolean,

    val isArchived: Boolean,

    val syncStatus:
    SyncStatusEntity,

    val remoteVersion: Long?,

    val lastSyncedAt: Long?,

    val isDeleted: Boolean =
        false,

    val deletedAt: Long? =
        null
)