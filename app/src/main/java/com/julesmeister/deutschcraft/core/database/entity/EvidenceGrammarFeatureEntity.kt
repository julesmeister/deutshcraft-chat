package com.julesmeister.deutschcraft.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.julesmeister.deutschcraft.core.cefr.model.GrammarFeature

@Entity(
    tableName = "evidence_grammar_features",
    primaryKeys = [
        "evidenceId",
        "grammarFeature"
    ],
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
        Index(value = ["grammarFeature"])
    ]
)
data class EvidenceGrammarFeatureEntity(
    val evidenceId: String,

    val grammarFeature: GrammarFeature,

    val demonstratedSuccessfully: Boolean,

    val confidence: Double
)