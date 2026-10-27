package com.julesmeister.deutschcraft.core.database.entity.besser

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "besser_challenge_hints",
    foreignKeys = [
        ForeignKey(
            entity = BesserChallengeEntity::class,
            parentColumns = [
                "id"
            ],
            childColumns = [
                "challenge_id"
            ],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["challenge_id"]
        ),
        Index(
            value = [
                "challenge_id",
                "position"
            ],
            unique = true
        )
    ]
)
data class BesserChallengeHintEntity(
    @PrimaryKey
    @ColumnInfo(
        name = "id"
    )
    val id: String,

    @ColumnInfo(
        name = "challenge_id"
    )
    val challengeId: String,

    @ColumnInfo(
        name = "position"
    )
    val position: Int,

    @ColumnInfo(
        name = "content"
    )
    val content: String
)