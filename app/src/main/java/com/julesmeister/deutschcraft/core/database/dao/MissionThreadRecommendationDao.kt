// app/src/main/java/com/julesmeister/deutschcraft/core/database/dao/MissionThreadRecommendationDao.kt

package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.julesmeister.deutschcraft.core.database.entity.mission.recommendation.MissionThreadRecommendationEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.recommendation.MissionThreadRecommendationObjectiveEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.recommendation.MissionThreadRecommendationWithObjectives
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionThreadRecommendationDao {

    @Query(
        """
        $BLOCKED_MISSION_SCENARIOS
        """
    )
    suspend fun getBlockedScenarioTypes(): List<String>

    @Query(
        """
        SELECT scenarioType FROM mission_scenario_completions
        UNION
        $COMPLETED_MISSION_SCENARIOS
        """
    )
    suspend fun getAllCompletedScenarioTypes(): List<String>

    @Query(
        """
        INSERT OR IGNORE INTO mission_scenario_completions (scenarioType)
        $COMPLETED_MISSION_SCENARIOS
        """
    )
    suspend fun recordCompletedScenarioTypes()

    @Query(
        """
        SELECT content FROM messages
        WHERE sender = 'USER'
          AND createdAt >= :minimumCreatedAt
          AND NOT EXISTS (
              SELECT 1 FROM conversation_missions m
              WHERE m.conversationId = messages.conversationId
          )
        ORDER BY createdAt DESC, sequenceNumber DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentOrdinaryLearnerMessages(minimumCreatedAt: Long, limit: Int): List<String>

    @Transaction
    @Query(
        """
        SELECT *
        FROM mission_thread_recommendations
        WHERE cefrLevel = :cefrLevel
          AND status IN ('READY', 'DISPLAYED')
          AND scenarioType NOT IN (
              $BLOCKED_MISSION_SCENARIOS
          )
          AND (
              expiresAt IS NULL
              OR expiresAt > :currentTime
          )
        ORDER BY
            CASE status
                WHEN 'DISPLAYED' THEN 0
                ELSE 1
            END,
            confidence DESC,
            createdAt ASC
        LIMIT 1
        """
    )
    fun observeNextEligibleRecommendation(
        cefrLevel: String,
        currentTime: Long
    ): Flow<MissionThreadRecommendationWithObjectives?>

    @Transaction
    @Query(
        """
        SELECT *
        FROM mission_thread_recommendations
        WHERE cefrLevel = :cefrLevel
          AND status IN ('READY', 'DISPLAYED')
          AND scenarioType NOT IN (
              $BLOCKED_MISSION_SCENARIOS
          )
          AND (
              expiresAt IS NULL
              OR expiresAt > :currentTime
          )
        ORDER BY
            CASE status
                WHEN 'DISPLAYED' THEN 0
                ELSE 1
            END,
            confidence DESC,
            createdAt ASC
        LIMIT 1
        """
    )
    suspend fun getNextEligibleRecommendation(
        cefrLevel: String,
        currentTime: Long
    ): MissionThreadRecommendationWithObjectives?

    @Transaction
    @Query(
        """
        SELECT *
        FROM mission_thread_recommendations
        WHERE cefrLevel = :cefrLevel
          AND status IN ('READY', 'DISPLAYED')
          AND scenarioType NOT IN (
              $BLOCKED_MISSION_SCENARIOS
          )
          AND (
              expiresAt IS NULL
              OR expiresAt > :currentTime
          )
        ORDER BY
            CASE status
                WHEN 'DISPLAYED' THEN 0
                ELSE 1
            END,
            confidence DESC,
            createdAt ASC
        LIMIT :limit
        """
    )
    suspend fun getEligibleRecommendations(
        cefrLevel: String,
        currentTime: Long,
        limit: Int
    ): List<MissionThreadRecommendationWithObjectives>

    @Transaction
    @Query(
        """
        SELECT *
        FROM mission_thread_recommendations
        WHERE cefrLevel = :cefrLevel
          AND status IN ('READY', 'DISPLAYED')
          AND scenarioType NOT IN (
              $BLOCKED_MISSION_SCENARIOS
          )
          AND (
              expiresAt IS NULL
              OR expiresAt > :currentTime
          )
        ORDER BY
            CASE status
                WHEN 'DISPLAYED' THEN 0
                ELSE 1
            END,
            confidence DESC,
            createdAt ASC
        """
    )
    suspend fun getAllEligibleRecommendations(
        cefrLevel: String,
        currentTime: Long
    ): List<MissionThreadRecommendationWithObjectives>

    @Query(
        """
        SELECT COUNT(*)
        FROM mission_thread_recommendations
        WHERE cefrLevel = :cefrLevel
          AND status IN ('READY', 'DISPLAYED')
          AND scenarioType NOT IN (
              $BLOCKED_MISSION_SCENARIOS
          )
          AND (
              expiresAt IS NULL
              OR expiresAt > :currentTime
          )
        """
    )
    suspend fun countEligibleRecommendations(
        cefrLevel: String,
        currentTime: Long
    ): Int

    @Query(
        """
        SELECT scenarioType
        FROM mission_thread_recommendations
        WHERE cefrLevel = :cefrLevel
          AND createdAt >= :minimumCreatedAt
          AND status IN (
              'READY',
              'DISPLAYED',
              'SKIPPED',
              'STARTED',
              'COMPLETED'
          )
        ORDER BY updatedAt ASC
        """
    )
    suspend fun getRecentScenarioTypes(
        cefrLevel: String,
        minimumCreatedAt: Long
    ): List<String>

    @Query(
        """
        SELECT scenarioType
        FROM mission_thread_recommendations
        WHERE cefrLevel = :cefrLevel
          AND completedAt IS NOT NULL
          AND completedAt >= :minimumCompletedAt
          AND status = 'COMPLETED'
        ORDER BY completedAt ASC
        """
    )
    suspend fun getRecentlyCompletedScenarioTypes(
        cefrLevel: String,
        minimumCompletedAt: Long
    ): List<String>

    @Query(
        """
        SELECT scenarioType
        FROM mission_thread_recommendations
        WHERE cefrLevel = :cefrLevel
          AND skippedAt IS NOT NULL
          AND skippedAt >= :minimumSkippedAt
          AND status = 'SKIPPED'
        ORDER BY skippedAt ASC
        """
    )
    suspend fun getRecentlyDismissedScenarioTypes(
        cefrLevel: String,
        minimumSkippedAt: Long
    ): List<String>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM mission_thread_recommendations
            WHERE cefrLevel = :cefrLevel
              AND scenarioType = :scenarioType
              AND status IN ('READY', 'DISPLAYED')
              AND (expiresAt IS NULL OR expiresAt > :currentTime)
        )
        OR :scenarioType IN (
            $BLOCKED_MISSION_SCENARIOS
        )
        """
    )
    suspend fun hasActiveScenario(
        cefrLevel: String,
        scenarioType: String,
        currentTime: Long
    ): Boolean

    @Insert(
        onConflict =
            OnConflictStrategy.ABORT
    )
    suspend fun insertRecommendation(
        recommendation:
        MissionThreadRecommendationEntity
    )

    @Insert(
        onConflict =
            OnConflictStrategy.ABORT
    )
    suspend fun insertObjectives(
        objectives:
        List<MissionThreadRecommendationObjectiveEntity>
    )

    @Transaction
    suspend fun insertCompleteRecommendation(
        recommendation:
        MissionThreadRecommendationEntity,

        objectives:
        List<MissionThreadRecommendationObjectiveEntity>
    ) {
        insertRecommendation(
            recommendation =
                recommendation
        )

        insertObjectives(
            objectives =
                objectives
        )
    }

    @Query(
        """
        UPDATE mission_thread_recommendations
        SET status = 'DISPLAYED',
            displayedAt = COALESCE(
                displayedAt,
                :displayedAt
            ),
            updatedAt = :displayedAt
        WHERE id = :recommendationId
          AND status = 'READY'
        """
    )
    suspend fun markDisplayed(
        recommendationId: String,
        displayedAt: Long
    ): Int

    @Query(
        """
        UPDATE mission_thread_recommendations
        SET status = 'SKIPPED',
            skippedAt = :skippedAt,
            updatedAt = :skippedAt
        WHERE id = :recommendationId
          AND status IN ('READY', 'DISPLAYED')
        """
    )
    suspend fun markSkipped(
        recommendationId: String,
        skippedAt: Long
    ): Int

    @Query(
        """
        UPDATE mission_thread_recommendations
        SET status = 'INVALID',
            updatedAt = :staleAt
        WHERE id = :recommendationId
          AND status IN ('READY', 'DISPLAYED')
        """
    )
    suspend fun markStale(
        recommendationId: String,
        staleAt: Long
    ): Int

    @Query(
        """
        UPDATE mission_thread_recommendations
        SET status = 'STARTED',
            startedAt = :startedAt,
            updatedAt = :startedAt,
            conversationId = :conversationId,
            conversationMissionId = :conversationMissionId
        WHERE id = :recommendationId
          AND status IN ('READY', 'DISPLAYED')
        """
    )
    suspend fun markStarted(
        recommendationId: String,
        conversationId: String,
        conversationMissionId: String,
        startedAt: Long
    ): Int

    @Query(
        """
        UPDATE mission_thread_recommendations
        SET status = 'COMPLETED',
            completedAt = :completedAt,
            updatedAt = :completedAt
        WHERE conversationMissionId = :conversationMissionId
        """
    )
    suspend fun markCompletedByMissionId(
        conversationMissionId: String,
        completedAt: Long
    ): Int

    @Query(
        """
        UPDATE mission_thread_recommendations
        SET status = 'EXPIRED',
            updatedAt = :currentTime
        WHERE status IN ('READY', 'DISPLAYED')
          AND expiresAt IS NOT NULL
          AND expiresAt <= :currentTime
        """
    )
    suspend fun expireOldRecommendations(
        currentTime: Long
    ): Int

    @Query(
        """
        UPDATE mission_thread_recommendations
        SET status = 'EXPIRED',
            updatedAt = :currentTime
        WHERE cefrLevel != :cefrLevel
          AND status IN ('READY', 'DISPLAYED')
        """
    )
    suspend fun expireOtherLevels(
        cefrLevel: String,
        currentTime: Long
    ): Int

    @Query(
        """
        DELETE FROM mission_thread_recommendations
        WHERE status IN (
            'SKIPPED',
            'EXPIRED',
            'INVALID'
        )
          AND updatedAt < :deleteBefore
        """
    )
    suspend fun deleteOldInactiveRecommendations(
        deleteBefore: Long
    ): Int
}

private const val BLOCKED_MISSION_SCENARIOS = """
    SELECT scenarioType FROM mission_scenario_completions
    UNION
    SELECT r.scenarioType
    FROM mission_thread_recommendations r
    LEFT JOIN conversation_missions m ON m.sourceRecommendationId = r.id
    WHERE r.status = 'COMPLETED' OR m.status IN ('ACTIVE', 'PAUSED', 'COMPLETED')
    UNION
    SELECT 'RESTAURANT_ORDERING'
    FROM conversation_missions m
    WHERE COALESCE(m.rootMissionTemplateId, m.missionTemplateId) = 'restaurant_ordering'
      AND m.status IN ('ACTIVE', 'PAUSED', 'COMPLETED')
    UNION
    SELECT r.scenarioType
    FROM mission_thread_recommendations r
    JOIN mission_templates t ON t.title = r.title AND t.scenario = r.scenario
        AND t.learnerRole = r.learnerRole AND t.assistantRole = r.assistantRole
    JOIN conversation_missions m
        ON COALESCE(m.rootMissionTemplateId, m.missionTemplateId) = t.id
    WHERE m.status IN ('ACTIVE', 'PAUSED', 'COMPLETED')
"""

internal const val COMPLETED_MISSION_SCENARIOS = """
    SELECT r.scenarioType
    FROM mission_thread_recommendations r
    LEFT JOIN conversation_missions m ON m.sourceRecommendationId = r.id
    WHERE r.status = 'COMPLETED' OR m.status = 'COMPLETED'
    UNION
    SELECT 'RESTAURANT_ORDERING'
    FROM conversation_missions m
    WHERE COALESCE(m.rootMissionTemplateId, m.missionTemplateId) = 'restaurant_ordering'
      AND m.status = 'COMPLETED'
    UNION
    SELECT r.scenarioType
    FROM mission_thread_recommendations r
    JOIN mission_templates t ON t.title = r.title AND t.scenario = r.scenario
        AND t.learnerRole = r.learnerRole AND t.assistantRole = r.assistantRole
    JOIN conversation_missions m
        ON COALESCE(m.rootMissionTemplateId, m.missionTemplateId) = t.id
    WHERE m.status = 'COMPLETED'
"""
