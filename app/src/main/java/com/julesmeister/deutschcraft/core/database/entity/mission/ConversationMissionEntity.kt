// app/src/main/java/com/julesmeister/deutschcraft/core/database/entity/mission/ConversationMissionEntity.kt

package com.julesmeister.deutschcraft.core.database.entity.mission

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity

@Entity(
    tableName =
        "conversation_missions",
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
        ),
        Index(
            value = [
                "sourceRecommendationId"
            ]
        )
    ]
)
data class ConversationMissionEntity(
    @PrimaryKey
    val id: String,

    val conversationId: String,

    val missionTemplateId: String,

    @ColumnInfo(
        defaultValue =
            "NULL"
    )
    val sourceRecommendationId: String? =
        null,

    @ColumnInfo(
        defaultValue =
            "'[]'"
    )
    val targetCapabilityIdsJson: String =
        "[]",

    @ColumnInfo(
        defaultValue =
            "'[]'"
    )
    val adaptiveFocusKeysJson: String =
        "[]",

    val status: String,

    val startedAt: Long?,

    val completedAt: Long?,

    val dismissedAt: Long?,

    val createdAt: Long,

    val updatedAt: Long
)