package com.julesmeister.deutschcraft.core.database.entity.mission

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity

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
            value = [
                "conversationId",
                "status"
            ]
        ),
        Index(
            value = ["sourceRecommendationId"]
        ),
        Index(
            value = ["rootMissionTemplateId"]
        ),
        Index(
            value = ["updatedAt"]
        ),
        Index(
            value = ["syncStatus"]
        ),
        Index(
            value = ["isDeleted"]
        )
    ]
)
data class ConversationMissionEntity(
    @PrimaryKey
    val id: String,

    val conversationId: String,

    val missionTemplateId: String,

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

    val updatedAt: Long,

    @ColumnInfo(
        defaultValue = "'LOCAL_ONLY'"
    )
    val syncStatus:
    SyncStatusEntity =
        SyncStatusEntity.LOCAL_ONLY,

    @ColumnInfo(
        defaultValue = "NULL"
    )
    val remoteVersion:
    Long? = null,

    @ColumnInfo(
        defaultValue = "NULL"
    )
    val lastSyncedAt:
    Long? = null,

    @ColumnInfo(
        defaultValue = "0"
    )
    val isDeleted:
    Boolean = false,

    @ColumnInfo(
        defaultValue = "NULL"
    )
    val deletedAt:
    Long? = null
)