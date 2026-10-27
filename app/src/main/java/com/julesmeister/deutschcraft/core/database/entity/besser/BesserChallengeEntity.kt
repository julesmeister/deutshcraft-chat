package com.julesmeister.deutschcraft.core.database.entity.besser

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "besser_challenges",
    foreignKeys = [
        ForeignKey(
            entity = CapabilityEntity::class,
            parentColumns = [
                "id"
            ],
            childColumns = [
                "capability_id"
            ],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(
            value = ["message_id"]
        ),
        Index(
            value = ["conversation_id"]
        ),
        Index(
            value = ["capability_id"]
        ),
        Index(
            value = ["status"]
        ),
        Index(
            value = [
                "message_id",
                "status"
            ]
        )
    ]
)
data class BesserChallengeEntity(
    @PrimaryKey
    @ColumnInfo(
        name = "id"
    )
    val id: String,

    @ColumnInfo(
        name = "message_id"
    )
    val messageId: String,

    @ColumnInfo(
        name = "conversation_id"
    )
    val conversationId: String,

    @ColumnInfo(
        name = "capability_id"
    )
    val capabilityId: String,

    @ColumnInfo(
        name = "outcome"
    )
    val outcome: String,

    @ColumnInfo(
        name = "instruction"
    )
    val instruction: String,

    @ColumnInfo(
        name = "reason_code"
    )
    val reasonCode: String,

    @ColumnInfo(
        name = "confidence"
    )
    val confidence: Float,

    @ColumnInfo(
        name = "recommended_assistance_level"
    )
    val recommendedAssistanceLevel: String,

    @ColumnInfo(
        name = "actual_assistance_level"
    )
    val actualAssistanceLevel: String? = null,

    @ColumnInfo(
        name = "status"
    )
    val status: String,

    @ColumnInfo(
        name = "revealed_hint_count"
    )
    val revealedHintCount: Int = 0,

    @ColumnInfo(
        name = "original_message_content"
    )
    val originalMessageContent: String,

    @ColumnInfo(
        name = "refined_message_content"
    )
    val refinedMessageContent: String? = null,

    @ColumnInfo(
        name = "created_at"
    )
    val createdAt: Long,

    @ColumnInfo(
        name = "accepted_at"
    )
    val acceptedAt: Long? = null,

    @ColumnInfo(
        name = "skipped_at"
    )
    val skippedAt: Long? = null,

    @ColumnInfo(
        name = "completed_at"
    )
    val completedAt: Long? = null,

    @ColumnInfo(
        name = "updated_at"
    )
    val updatedAt: Long
)