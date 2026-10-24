package com.julesmeister.deutschcraft.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.deutschcraft.core.cefr.model.AssessmentSource
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

@Entity(
    tableName = "assessment_evidence",
    foreignKeys = [
        ForeignKey(
            entity = LearnerProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId"]),
        Index(
            value = ["messageId"],
            unique = true
        ),
        Index(value = ["conversationId"]),
        Index(value = ["assessedLevel"]),
        Index(value = ["analyzedAt"]),
        Index(value = ["source"]),
        Index(value = ["syncStatus"])
    ]
)
data class AssessmentEvidenceEntity(
    @PrimaryKey
    val id: String,

    val profileId: String,

    val messageId: String,

    val conversationId: String,

    val assessedLevel: CefrLevel,

    val grammarScore: Int,

    val vocabularyScore: Int,

    val coherenceScore: Int,

    val accuracyScore: Int,

    val rangeScore: Int,

    val analyzedAt: Long,

    val source: AssessmentSource,

    val ruleVersion: Int,

    val syncStatus: SyncStatusEntity,

    val remoteVersion: Long?,

    val lastSyncedAt: Long?
)