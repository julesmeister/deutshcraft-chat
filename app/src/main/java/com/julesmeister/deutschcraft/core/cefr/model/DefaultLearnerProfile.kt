package com.julesmeister.deutschcraft.core.cefr.model

import com.julesmeister.deutschcraft.core.cefr.rules.CefrRuleVersion
import com.julesmeister.deutschcraft.core.database.entity.LearnerProfileEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity

object DefaultLearnerProfile {

    const val LOCAL_PROFILE_ID =
        "local-learner-profile"

    fun createEntity(
        currentTime: Long
    ): LearnerProfileEntity {
        return LearnerProfileEntity(
            id = LOCAL_PROFILE_ID,
            currentLevel = CefrLevel.A1,
            overallScore = 0,
            readinessPercentage = 0,
            assessedMessageCount = 0,
            activeDayCount = 0,
            recentCriticalErrorRate = 0.0,
            consecutiveQualifiedAssessments = 0,
            progressionState =
                ProgressionState.BUILDING_EVIDENCE,
            suggestedNextLevel = null,
            levelAcceptedAt = null,
            lastAssessedAt = null,
            createdAt = currentTime,
            updatedAt = currentTime,
            assessmentRuleVersion =
                CefrRuleVersion.CURRENT,
            syncStatus = SyncStatusEntity.LOCAL_ONLY,
            remoteVersion = null,
            lastSyncedAt = null
        )
    }
}