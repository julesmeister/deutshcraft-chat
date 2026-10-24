package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.cefr.model.ProgressionState
import com.julesmeister.deutschcraft.core.database.entity.LearnerProfileEntity
import com.julesmeister.deutschcraft.core.database.entity.SkillScoreEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import com.julesmeister.deutschcraft.core.database.relation.LearnerProfileWithSkills
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LearnerProfileDao {

    @Upsert
    abstract suspend fun upsertProfile(
        profile: LearnerProfileEntity
    )

    @Upsert
    abstract suspend fun upsertSkillScores(
        skillScores: List<SkillScoreEntity>
    )

    @Transaction
    @Query(
        """
        SELECT *
        FROM learner_profiles
        WHERE id = :profileId
        LIMIT 1
        """
    )
    abstract fun observeProfileWithSkills(
        profileId: String
    ): Flow<LearnerProfileWithSkills?>

    @Transaction
    @Query(
        """
        SELECT *
        FROM learner_profiles
        WHERE id = :profileId
        LIMIT 1
        """
    )
    abstract suspend fun getProfileWithSkills(
        profileId: String
    ): LearnerProfileWithSkills?

    @Query(
        """
        SELECT *
        FROM learner_profiles
        WHERE id = :profileId
        LIMIT 1
        """
    )
    abstract suspend fun getProfile(
        profileId: String
    ): LearnerProfileEntity?

    @Query(
        """
        SELECT *
        FROM skill_scores
        WHERE profileId = :profileId
        """
    )
    abstract suspend fun getSkillScores(
        profileId: String
    ): List<SkillScoreEntity>

    @Query(
        """
        DELETE FROM skill_scores
        WHERE profileId = :profileId
        """
    )
    abstract suspend fun deleteSkillScores(
        profileId: String
    )

    @Transaction
    open suspend fun replaceProfileWithSkills(
        profile: LearnerProfileEntity,
        skillScores: List<SkillScoreEntity>
    ) {
        upsertProfile(profile)

        deleteSkillScores(profile.id)

        upsertSkillScores(skillScores)
    }

    @Query(
        """
        UPDATE learner_profiles
        SET
            currentLevel = :currentLevel,
            overallScore = :overallScore,
            readinessPercentage = :readinessPercentage,
            assessedMessageCount = :assessedMessageCount,
            activeDayCount = :activeDayCount,
            recentCriticalErrorRate = :recentCriticalErrorRate,
            consecutiveQualifiedAssessments =
                :consecutiveQualifiedAssessments,
            progressionState = :progressionState,
            suggestedNextLevel = :suggestedNextLevel,
            lastAssessedAt = :lastAssessedAt,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        WHERE id = :profileId
        """
    )
    abstract suspend fun updateProfile(
        profileId: String,
        currentLevel: CefrLevel,
        overallScore: Int,
        readinessPercentage: Int,
        assessedMessageCount: Int,
        activeDayCount: Int,
        recentCriticalErrorRate: Double,
        consecutiveQualifiedAssessments: Int,
        progressionState: ProgressionState,
        suggestedNextLevel: CefrLevel?,
        lastAssessedAt: Long?,
        updatedAt: Long,
        syncStatus: SyncStatusEntity
    )
}