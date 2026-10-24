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

    /*
     * Basic profile operations
     */

    @Upsert
    abstract suspend fun upsertProfile(
        profile: LearnerProfileEntity
    )

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
        FROM learner_profiles
        WHERE id = :profileId
        LIMIT 1
        """
    )
    abstract fun observeProfile(
        profileId: String
    ): Flow<LearnerProfileEntity?>

    /*
     * Profile and skill-score relations
     */

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

    /*
     * Skill score operations
     */

    @Upsert
    abstract suspend fun upsertSkillScore(
        skillScore: SkillScoreEntity
    )

    @Upsert
    abstract suspend fun upsertSkillScores(
        skillScores: List<SkillScoreEntity>
    )

    @Query(
        """
        SELECT *
        FROM skill_scores
        WHERE profileId = :profileId
        ORDER BY skill ASC
        """
    )
    abstract fun observeSkillScores(
        profileId: String
    ): Flow<List<SkillScoreEntity>>

    @Query(
        """
        SELECT *
        FROM skill_scores
        WHERE profileId = :profileId
        ORDER BY skill ASC
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

    /*
     * Atomic profile replacement
     */

    @Transaction
    open suspend fun replaceProfileWithSkills(
        profile: LearnerProfileEntity,
        skillScores: List<SkillScoreEntity>
    ) {
        require(
            skillScores.all { score ->
                score.profileId == profile.id
            }
        ) {
            "Every skill score must belong to profile ${profile.id}"
        }

        upsertProfile(profile)

        // There are only a small number of skill rows.
        // Replacing them prevents stale skills from remaining.
        deleteSkillScores(profile.id)

        if (skillScores.isNotEmpty()) {
            upsertSkillScores(skillScores)
        }
    }

    /*
     * Progress updates
     */

    @Query(
        """
        UPDATE learner_profiles
        SET
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
            assessmentRuleVersion = :ruleVersion,
            syncStatus = :syncStatus
        WHERE id = :profileId
        """
    )
    abstract suspend fun updateAssessmentSummary(
        profileId: String,
        overallScore: Int,
        readinessPercentage: Int,
        assessedMessageCount: Int,
        activeDayCount: Int,
        recentCriticalErrorRate: Double,
        consecutiveQualifiedAssessments: Int,
        progressionState: ProgressionState,
        suggestedNextLevel: CefrLevel?,
        lastAssessedAt: Long,
        updatedAt: Long,
        ruleVersion: Int,
        syncStatus: SyncStatusEntity
    ): Int

    @Query(
        """
        UPDATE learner_profiles
        SET
            currentLevel = :newLevel,
            readinessPercentage = 0,
            consecutiveQualifiedAssessments = 0,
            progressionState = :progressionState,
            suggestedNextLevel = NULL,
            levelAcceptedAt = :acceptedAt,
            updatedAt = :acceptedAt,
            syncStatus = :syncStatus
        WHERE id = :profileId
        """
    )
    abstract suspend fun acceptLevelProgression(
        profileId: String,
        newLevel: CefrLevel,
        progressionState: ProgressionState,
        acceptedAt: Long,
        syncStatus: SyncStatusEntity
    ): Int

    @Query(
        """
        UPDATE learner_profiles
        SET
            progressionState = :progressionState,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        WHERE id = :profileId
        """
    )
    abstract suspend fun updateProgressionState(
        profileId: String,
        progressionState: ProgressionState,
        updatedAt: Long,
        syncStatus: SyncStatusEntity
    ): Int

    /*
     * Synchronization operations
     */

    @Query(
        """
        SELECT *
        FROM learner_profiles
        WHERE syncStatus IN (
            'PENDING_UPLOAD',
            'PENDING_DELETE'
        )
        ORDER BY updatedAt ASC
        """
    )
    abstract suspend fun getProfilesPendingSync():
            List<LearnerProfileEntity>

    @Query(
        """
        UPDATE learner_profiles
        SET
            syncStatus = :syncStatus,
            remoteVersion = :remoteVersion,
            lastSyncedAt = :lastSyncedAt
        WHERE id = :profileId
        """
    )
    abstract suspend fun markProfileSynced(
        profileId: String,
        syncStatus: SyncStatusEntity,
        remoteVersion: Long?,
        lastSyncedAt: Long
    ): Int

    /*
     * Destructive operations
     */

    @Query(
        """
        DELETE FROM learner_profiles
        WHERE id = :profileId
        """
    )
    abstract suspend fun deleteProfile(
        profileId: String
    ): Int

    @Query("DELETE FROM learner_profiles")
    abstract suspend fun deleteAllProfiles()
}
