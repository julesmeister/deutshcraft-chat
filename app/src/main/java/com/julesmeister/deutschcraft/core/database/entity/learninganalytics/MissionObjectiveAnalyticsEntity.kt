package com.julesmeister.deutschcraft.core.database.entity.learninganalytics

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName =
        "mission_objective_analytics",
    primaryKeys = [
        "missionId",
        "objectiveId"
    ],
    foreignKeys = [
        ForeignKey(
            entity =
                MissionAnalyticsEntity::class,
            parentColumns = [
                "missionId"
            ],
            childColumns = [
                "missionId"
            ],
            onDelete =
                ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = [
                "missionId"
            ]
        )
    ]
)
data class MissionObjectiveAnalyticsEntity(
    val missionId: String,

    val objectiveId: String,

    val title: String,

    val isRequired: Boolean,

    val wasCompleted: Boolean,

    /*
     * 0 = independent
     * 1 = gentle support
     * 2 = substantial support
     * 3 = direct support
     */
    val hintsUsed: Int,

    val besserCorrectionCount: Int,

    val evaluationConfidence: Float,

    /*
     * JSON arrays.
     */
    val targetVocabularyJson: String,

    val correctlyUsedVocabularyJson: String
)