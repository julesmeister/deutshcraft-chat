// app/src/main/java/com/julesmeister/deutschcraft/core/database/entity/mission/ConversationMissionEntity.kt

package com.julesmeister.deutschcraft.core.database.entity.mission

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity

@Entity(
    tableName = "conversation_missions",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MissionTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["missionTemplateId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(
            value = ["conversationId"]
        ),
        Index(
            value = ["missionTemplateId"]
        ),
        Index(
            value = ["conversationId", "status"]
        ),
        Index(
            value = ["sourceRecommendationId"]
        ),
        Index(
            value = ["rootMissionTemplateId"]
        )
    ]
)
data class ConversationMissionEntity(
    @PrimaryKey
    val id: String,

    val conversationId: String,

    /*
     * The template currently driving the active branch.
     *
     * This may change when the learner selects another
     * direction.
     */
    val missionTemplateId: String,

    /*
     * Immutable semantic anchor for the mission attempt.
     *
     * For rows created before database version 18, this is
     * initially null and falls back to missionTemplateId.
     *
     * The first branch operation persists the original
     * missionTemplateId here before replacing the current
     * branch template.
     */
    @ColumnInfo(
        defaultValue = "NULL"
    )
    val rootMissionTemplateId: String? = null,

    @ColumnInfo(
        defaultValue = "NULL"
    )
    val sourceRecommendationId: String? = null,

    @ColumnInfo(
        defaultValue = "'[]'"
    )
    val targetCapabilityIdsJson: String = "[]",

    @ColumnInfo(
        defaultValue = "'[]'"
    )
    val adaptiveFocusKeysJson: String = "[]",

    val status: String,

    val startedAt: Long?,

    val completedAt: Long?,

    val dismissedAt: Long?,

    val createdAt: Long,

    val updatedAt: Long
)