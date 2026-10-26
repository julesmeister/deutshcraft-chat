package com.julesmeister.deutschcraft.core.database.entity.mission

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "mission_objective_progress",
    primaryKeys = [
        "conversationMissionId",
        "objectiveId"
    ],
    foreignKeys = [
        ForeignKey(
            entity =
                ConversationMissionEntity::class,
            parentColumns = [
                "id"
            ],
            childColumns = [
                "conversationMissionId"
            ],
            onDelete =
                ForeignKey.CASCADE
        ),
        ForeignKey(
            entity =
                MissionObjectiveEntity::class,
            parentColumns = [
                "id"
            ],
            childColumns = [
                "objectiveId"
            ],
            onDelete =
                ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = [
                "conversationMissionId"
            ]
        ),
        Index(
            value = [
                "objectiveId"
            ]
        ),
        Index(
            value = [
                "completedByMessageId"
            ]
        )
    ]
)
data class MissionObjectiveProgressEntity(
    val conversationMissionId: String,
    val objectiveId: String,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val completedByMessageId: String?,
    val confidence: Float,
    val updatedAt: Long
)