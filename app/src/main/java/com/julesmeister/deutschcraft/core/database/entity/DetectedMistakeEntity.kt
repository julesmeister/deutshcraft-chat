package com.julesmeister.deutschcraft.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.deutschcraft.core.cefr.model.GrammarFeature
import com.julesmeister.deutschcraft.core.cefr.model.MistakeCategory
import com.julesmeister.deutschcraft.core.cefr.model.MistakeSeverity

@Entity(
    tableName = "detected_mistakes",
    foreignKeys = [
        ForeignKey(
            entity = AssessmentEvidenceEntity::class,
            parentColumns = ["id"],
            childColumns = ["evidenceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["evidenceId"]),
        Index(value = ["category"]),
        Index(value = ["grammarFeature"]),
        Index(value = ["severity"]),
        Index(value = ["detectedAt"]),
        Index(value = ["isResolved"])
    ]
)
data class DetectedMistakeEntity(
    @PrimaryKey
    val id: String,

    val evidenceId: String,

    val category: MistakeCategory,

    val grammarFeature: GrammarFeature?,

    val originalText: String?,

    val correctedText: String?,

    val explanation: String?,

    val severity: MistakeSeverity,

    val occurrenceCount: Int,

    val isResolved: Boolean,

    val detectedAt: Long,

    val lastObservedAt: Long
)