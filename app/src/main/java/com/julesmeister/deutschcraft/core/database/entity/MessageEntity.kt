package com.julesmeister.deutschcraft.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "messages",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["conversationId"]
        ),
        Index(
            value = [
                "conversationId",
                "sequenceNumber"
            ],
            unique = true
        )
    ]
)
data class MessageEntity(
    val id: String,
    val conversationId: String,
    val content: String,
    val sender: MessageSenderEntity,
    val sequenceNumber: Long,
    val createdAt: Long,

    /*
     * The complete corrected German sentence.
     *
     * null means the learner's original sentence does
     * not require a meaningful correction.
     */
    val correction: String? = null,

    val syncStatus: SyncStatusEntity,
    val remoteVersion: Long?,
    val lastSyncedAt: Long?
)