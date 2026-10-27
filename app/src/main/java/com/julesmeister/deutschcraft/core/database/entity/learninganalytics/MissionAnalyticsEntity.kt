package com.julesmeister.deutschcraft.core.database.entity.learninganalytics

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName =
        "mission_analytics",
    indices = [
        Index(
            value = [
                "conversationId"
            ]
        ),
        Index(
            value = [
                "cefrLevel",
                "completedAt"
            ]
        )
    ]
)
data class MissionAnalyticsEntity(
    @PrimaryKey
    val missionId: String,

    val conversationId: String,

    val cefrLevel: String,

    val completedObjectiveCount: Int,

    val totalObjectiveCount: Int,

    val completedRequiredObjectiveCount: Int,

    val totalRequiredObjectiveCount: Int,

    val overallScore: Int,

    val independenceScore: Int,

    val grammarScore: Int,

    val vocabularyScore: Int,

    val comprehensionScore: Int,

    val interactionScore: Int,

    val hintsUsed: Int,

    val besserCorrectionCount: Int,

    val resolvedBesserCorrectionCount: Int,

    val independentlyCompletedObjectiveCount: Int,

    val supportedObjectiveCount: Int,

    val directlyAssistedObjectiveCount: Int,

    val conversationScore: Int,

    val createdAt: Long,

    val completedAt: Long
)