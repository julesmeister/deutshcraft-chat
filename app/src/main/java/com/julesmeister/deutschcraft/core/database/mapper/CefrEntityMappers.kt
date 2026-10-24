package com.julesmeister.deutschcraft.core.database.mapper

import com.julesmeister.deutschcraft.core.cefr.model.AssessmentEvidence
import com.julesmeister.deutschcraft.core.cefr.model.CefrProfile
import com.julesmeister.deutschcraft.core.cefr.model.DetectedMistake
import com.julesmeister.deutschcraft.core.cefr.model.SkillScore
import com.julesmeister.deutschcraft.core.database.entity.AssessmentEvidenceEntity
import com.julesmeister.deutschcraft.core.database.entity.DetectedMistakeEntity
import com.julesmeister.deutschcraft.core.database.entity.EvidenceGrammarFeatureEntity
import com.julesmeister.deutschcraft.core.database.entity.LearnerProfileEntity
import com.julesmeister.deutschcraft.core.database.entity.SkillScoreEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import com.julesmeister.deutschcraft.core.database.relation.AssessmentEvidenceWithDetails
import com.julesmeister.deutschcraft.core.database.relation.LearnerProfileWithSkills
import java.util.UUID

fun LearnerProfileWithSkills.toDomain(): CefrProfile {
    return CefrProfile(
        currentLevel = profile.currentLevel,
        skillScores = skillScores.associate { entity ->
            entity.skill to entity.toDomain()
        },
        overallScore = profile.overallScore,
        readinessPercentage = profile.readinessPercentage,
        assessedMessageCount = profile.assessedMessageCount,
        activeDayCount = profile.activeDayCount,
        recentCriticalErrorRate =
            profile.recentCriticalErrorRate,
        consecutiveQualifiedAssessments =
            profile.consecutiveQualifiedAssessments,
        progressionState = profile.progressionState,
        suggestedNextLevel = profile.suggestedNextLevel,
        lastAssessedAt = profile.lastAssessedAt ?: 0L
    )
}

fun SkillScoreEntity.toDomain(): SkillScore {
    return SkillScore(
        skill = skill,
        score = score,
        evidenceCount = evidenceCount,
        confidence = confidence
    )
}

fun SkillScore.toEntity(
    profileId: String,
    updatedAt: Long
): SkillScoreEntity {
    return SkillScoreEntity(
        profileId = profileId,
        skill = skill,
        score = score,
        evidenceCount = evidenceCount,
        confidence = confidence,
        updatedAt = updatedAt
    )
}

fun AssessmentEvidenceWithDetails.toDomain():
        AssessmentEvidence {
    return AssessmentEvidence(
        id = evidence.id,
        messageId = evidence.messageId,
        conversationId = evidence.conversationId,
        assessedLevel = evidence.assessedLevel,
        grammarScore = evidence.grammarScore,
        vocabularyScore = evidence.vocabularyScore,
        coherenceScore = evidence.coherenceScore,
        accuracyScore = evidence.accuracyScore,
        rangeScore = evidence.rangeScore,
        detectedGrammarFeatures = grammarFeatures
            .map { entity ->
                entity.grammarFeature
            }
            .toSet(),
        mistakes = mistakes.map(
            DetectedMistakeEntity::toDomain
        ),
        analyzedAt = evidence.analyzedAt,
        source = evidence.source,
        ruleVersion = evidence.ruleVersion
    )
}

fun DetectedMistakeEntity.toDomain():
        DetectedMistake {
    return DetectedMistake(
        category = category,
        grammarFeature = grammarFeature,
        originalText = originalText,
        correctedText = correctedText,
        severity = severity
    )
}

fun AssessmentEvidence.toEntity(
    profileId: String
): AssessmentEvidenceEntity {
    return AssessmentEvidenceEntity(
        id = id,
        profileId = profileId,
        messageId = messageId,
        conversationId = conversationId,
        assessedLevel = assessedLevel,
        grammarScore = grammarScore,
        vocabularyScore = vocabularyScore,
        coherenceScore = coherenceScore,
        accuracyScore = accuracyScore,
        rangeScore = rangeScore,
        analyzedAt = analyzedAt,
        source = source,
        ruleVersion = ruleVersion,
        syncStatus = SyncStatusEntity.LOCAL_ONLY,
        remoteVersion = null,
        lastSyncedAt = null
    )
}

fun AssessmentEvidence.toGrammarFeatureEntities():
        List<EvidenceGrammarFeatureEntity> {
    return detectedGrammarFeatures.map { feature ->
        EvidenceGrammarFeatureEntity(
            evidenceId = id,
            grammarFeature = feature,
            demonstratedSuccessfully = mistakes.none {
                    mistake ->
                mistake.grammarFeature == feature &&
                        mistake.severity !=
                        com.julesmeister.deutschcraft
                            .core.cefr.model.MistakeSeverity.MINOR
            },
            confidence = 0.5
        )
    }
}

fun AssessmentEvidence.toMistakeEntities():
        List<DetectedMistakeEntity> {
    return mistakes.map { mistake ->
        DetectedMistakeEntity(
            id = UUID.randomUUID().toString(),
            evidenceId = id,
            category = mistake.category,
            grammarFeature = mistake.grammarFeature,
            originalText = mistake.originalText,
            correctedText = mistake.correctedText,
            explanation = null,
            severity = mistake.severity,
            occurrenceCount = 1,
            isResolved = false,
            detectedAt = analyzedAt,
            lastObservedAt = analyzedAt
        )
    }
}