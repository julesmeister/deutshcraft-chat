package com.julesmeister.deutschcraft.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.julesmeister.deutschcraft.core.database.entity.AssessmentEvidenceEntity
import com.julesmeister.deutschcraft.core.database.entity.DetectedMistakeEntity
import com.julesmeister.deutschcraft.core.database.entity.EvidenceGrammarFeatureEntity

data class AssessmentEvidenceWithDetails(
    @Embedded
    val evidence: AssessmentEvidenceEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "evidenceId"
    )
    val grammarFeatures:
    List<EvidenceGrammarFeatureEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "evidenceId"
    )
    val mistakes:
    List<DetectedMistakeEntity>
)