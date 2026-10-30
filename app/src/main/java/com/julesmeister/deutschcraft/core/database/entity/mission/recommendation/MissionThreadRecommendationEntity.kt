// app/src/main/java/com/julesmeister/deutschcraft/core/database/entity/mission/recommendation/MissionThreadRecommendationEntity.kt

package com.julesmeister.deutschcraft.core.database.entity.mission.recommendation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName =
        "mission_thread_recommendations",
    indices = [
        Index(
            value = [
                "cefrLevel",
                "status",
                "createdAt"
            ]
        ),
        Index(
            value = [
                "scenarioType"
            ]
        ),
        Index(
            value = [
                "status",
                "expiresAt"
            ]
        )
    ]
)
data class MissionThreadRecommendationEntity(
    @PrimaryKey
    val id: String,

    val scenarioType: String,

    val cefrLevel: String,

    val title: String,

    val previewText: String,

    val confidence: Float,

    val reason: String,

    val description: String,

    val scenario: String,

    val learnerRole: String,

    val assistantRole: String,

    val icon: String,

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

    @ColumnInfo(
        defaultValue =
            "'[]'"
    )
    val masteredCapabilityIdsJson: String =
        "[]",

    val status: String,

    val createdAt: Long,

    val updatedAt: Long,

    val displayedAt: Long?,

    val skippedAt: Long?,

    val startedAt: Long?,

    val completedAt: Long?,

    val expiresAt: Long?,

    val conversationId: String?,

    val conversationMissionId: String?
)