package com.julesmeister.deutschcraft.core.database.entity.mission

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity

@Entity(
    tableName = "conversation_missions",
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
        ),
        ForeignKey(
            entity =
                MissionTemplateEntity::class,
            parentColumns = [
                "id"
            ],
            childColumns = [
                "missionTemplateId"
            ],
            onDelete =
                ForeignKey.RESTRICT
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
                "missionTemplateId"
            ]
        ),
        Index(
            value = [
                "conversationId",
                "status"
            ]
        )
    ]
)
data class ConversationMissionEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val missionTemplateId: String,
    val status: String,
    val startedAt: Long?,
    val completedAt: Long?,
    val dismissedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
)