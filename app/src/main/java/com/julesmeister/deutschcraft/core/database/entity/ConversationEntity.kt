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
        Index(value = ["syncStatus"])
    ]
)
data class ConversationEntity(
    @PrimaryKey
    val id: String,

    val title: String,

    val titleSource: ConversationTitleSource,

    val cefrLevel: CefrLevel,

    /**
     * ISO local date such as "2026-10-24".
     */
    val localDate: String,

    /**
     * LocalDate.toEpochDay(), used for daily
     * conversation lookup and grouping.
     */
    val epochDay: Long,

    val createdAt: Long,

    val updatedAt: Long,

    /**
     * Timestamp when this thread was closed.
     *
     * Null means that the thread is still open.
     */
    val closedAt: Long?,

    val messageCount: Int,

    val previewText: String?,

    val isClosed: Boolean,

    val isArchived: Boolean,

    val syncStatus: SyncStatusEntity,

    val remoteVersion: Long?,

    val lastSyncedAt: Long?
)