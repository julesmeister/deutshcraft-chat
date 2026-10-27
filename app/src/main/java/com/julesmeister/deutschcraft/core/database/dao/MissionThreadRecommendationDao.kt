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

    /*
     * Observes the first recommendation eligible for display.
     *
     * A currently DISPLAYED recommendation has priority over
     * unseen READY recommendations.
     */
    @Transaction
    @Query(
        """
        SELECT *
        FROM mission_thread_recommendations
        WHERE cefrLevel = :cefrLevel
          AND status IN ('READY', 'DISPLAYED')
          AND (
              expiresAt IS NULL
              OR expiresAt > :currentTime
          )
        ORDER BY
            CASE status
                WHEN 'DISPLAYED' THEN 0
                ELSE 1
            END,
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
          AND (
              expiresAt IS NULL
              OR expiresAt > :currentTime
          )
        ORDER BY
            CASE status
                WHEN 'DISPLAYED' THEN 0
                ELSE 1
            END,
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
          AND (
              expiresAt IS NULL
              OR expiresAt > :currentTime
          )
        ORDER BY
            CASE status
                WHEN 'DISPLAYED' THEN 0
                ELSE 1
            END,
            createdAt ASC
        LIMIT :limit
        """
    )
    suspend fun getEligibleRecommendations(
        cefrLevel: String,
        currentTime: Long,
        limit: Int
    ): List<MissionThreadRecommendationWithObjectives>

    @Query(
        """
        SELECT COUNT(*)
        FROM mission_thread_recommendations
        WHERE cefrLevel = :cefrLevel
          AND status IN ('READY', 'DISPLAYED')
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

    /*
     * Returns scenario identities that must be excluded from
     * generation.
     *
     * This includes:
     *
     * - Waiting recommendations
     * - Displayed recommendations
     * - Skipped recommendations
     * - Started recommendations
     * - Recently completed recommendations
     */
    @Query(
        """
        SELECT DISTINCT scenarioType
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
        ORDER BY createdAt DESC
        """
    )
    suspend fun getRecentScenarioTypes(
        cefrLevel: String,
        minimumCreatedAt: Long
    ): List<String>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM mission_thread_recommendations
            WHERE cefrLevel = :cefrLevel
              AND scenarioType = :scenarioType
              AND status IN (
                  'READY',
                  'DISPLAYED',
                  'STARTED'
              )
        )
        """
    )
    suspend fun hasActiveScenario(
        cefrLevel: String,
        scenarioType: String
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

    /*
     * Persists the complete generated recommendation as one
     * Room transaction.
     */
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
        SET status = 'STARTED',
            startedAt = :startedAt,
            updatedAt = :startedAt,
            conversationId = :conversationId,
            conversationMissionId =
                :conversationMissionId
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
        WHERE conversationMissionId =
            :conversationMissionId
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