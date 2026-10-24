package com.julesmeister.deutschcraft.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.julesmeister.deutschcraft.core.cefr.model.CefrSkill

@Entity(
    tableName = "skill_scores",
    primaryKeys = [
        "profileId",
        "skill"
    ],
    foreignKeys = [
        ForeignKey(
            entity = LearnerProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["skill"]),
        Index(value = ["updatedAt"])
    ]
)
data class SkillScoreEntity(
    val profileId: String,

    val skill: CefrSkill,

    val score: Int,

    val evidenceCount: Int,

    val confidence: Double,

    val updatedAt: Long
)