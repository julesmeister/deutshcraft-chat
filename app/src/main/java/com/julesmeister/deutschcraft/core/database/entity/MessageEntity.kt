// app/src/main/java/com/julesmeister/deutschcraft/core/database/entity/MessageEntity.kt

package com.julesmeister.deutschcraft.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "messages",
    primaryKeys = [
        "id"
    ],
    foreignKeys = [
        ForeignKey(
            entity =
                ConversationEntity::class,

            parentColumns = [
                "id"
            ],

            childColumns = [
                "conversationId"
            ],

            onDelete =
                ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = [
                "conversationId"
            ]
        ),
        Index(
            value = [
                "conversationId",
                "sequenceNumber"
            ],
            unique = true
        ),
        Index(
            value = [
                "updatedAt"
            ]
        ),
        Index(
            value = [
                "isDeleted"
            ]
        ),
        Index(
            value = [
                "syncStatus"
            ]
        )
    ]
)
data class MessageEntity(
    val id: String,

    val conversationId: String,

    val content: String,

    val sender:
    MessageSenderEntity,

    val sequenceNumber: Long,

    val createdAt: Long,

    val correction: String? =
        null,

    val syncStatus:
    SyncStatusEntity,

    val remoteVersion: Long?,

    val lastSyncedAt: Long?,

    val updatedAt: Long =
        createdAt,

    val isDeleted: Boolean =
        false,

    val deletedAt: Long? =
        null
)