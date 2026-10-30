// app/src/main/java/com/julesmeister/deutschcraft/core/database/entity/besser/MissionCapabilityEvidenceEntity.kt

package com.julesmeister.deutschcraft.core.database.entity.besser

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName =
        "mission_capability_evidence",
    primaryKeys = [
        "missionId",
        "capabilityId"
    ],
    indices = [
        Index(
            value = [
                "missionId"
            ]
        ),
        Index(
            value = [
                "conversationId"
            ]
        ),
        Index(
            value = [
                "capabilityId"
            ]
        ),
        Index(
            value = [
                "capabilityId",
                "evaluatedAt"
            ]
        )
    ]
)
data class MissionCapabilityEvidenceEntity(
    val missionId: String,

    val conversationId: String,

    val capabilityId: String,

    val overallScore: Int,

    val independenceScore: Int,

    val objectiveCompletionPercentage: Int,

    val evidenceScore: Int,

    val confidence: Float,

    val demonstrated: Boolean,

    val evaluatedAt: Long
)