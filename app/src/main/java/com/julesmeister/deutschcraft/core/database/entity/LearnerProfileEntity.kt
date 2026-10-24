package com.julesmeister.deutschcraft.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.cefr.model.ProgressionState

@Entity(
    tableName = "learner_profiles",
    indices = [
        Index(value = ["currentLevel"]),
        Index(value = ["progressionState"]),
        Index(value = ["updatedAt"]),
        Index(value = ["syncStatus"])
    ]
)
data class LearnerProfileEntity(
    @PrimaryKey
    val id: String,

    val currentLevel: CefrLevel,

    val overallScore: Int,

    val readinessPercentage: Int,

    val assessedMessageCount: Int,

    val activeDayCount: Int,

    val recentCriticalErrorRate: Double,

    val consecutiveQualifiedAssessments: Int,

    val progressionState: ProgressionState,

    val suggestedNextLevel: CefrLevel?,

    val levelAcceptedAt: Long?,

    val lastAssessedAt: Long?,

    val createdAt: Long,

    val updatedAt: Long,

    val assessmentRuleVersion: Int,

    val syncStatus: SyncStatusEntity,

    val remoteVersion: Long?,

    val lastSyncedAt: Long?
)