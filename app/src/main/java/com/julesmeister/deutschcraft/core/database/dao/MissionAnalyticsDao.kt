package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionAnalyticsEntity
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionAnalyticsWithObjectives
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionObjectiveAnalyticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionAnalyticsDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun upsertMissionAnalytics(
        analytics:
        MissionAnalyticsEntity
    )

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun upsertObjectiveAnalytics(
        analytics:
        List<MissionObjectiveAnalyticsEntity>
    )

    /*
     * Save the entire final mission-analysis result in one
     * transaction.
     */
    @Transaction
    suspend fun saveCompleteAnalytics(
    analytics:
    MissionAnalyticsEntity,
    objectives:
      List<MissionObjectiveAnalyticsEntity>
    ) {
        upsertMissionAnalytics(analytics = analytics)

        upsertObjectiveAnalytics(analytics = objectives)
    }
    @Transaction
    @Query(
           """
        SELECT *
        FROM mission_analytics
        WHERE missionId = :missionId
        LIMIT 1
        """
    )
    suspend fun getMissionAnalytics(
        missionId: String
    ): MissionAnalyticsWithObjectives?

    @Transaction
    @Query(
        """
        SELECT *
        FROM mission_analytics
        WHERE conversationId = :conversationId
        ORDER BY completedAt DESC
        """
    )
    fun observeConversationAnalytics(
        conversationId: String
    ): Flow<List<MissionAnalyticsWithObjectives>>

    /*
     * Feeds CalculateCefrProgressUseCase.
     *
     * We intentionally fetch recent evidence rather than the
     * learner's entire lifetime.
     */
    @Transaction
    @Query(
        """
        SELECT *
        FROM mission_analytics
        WHERE cefrLevel = :cefrLevel
        ORDER BY completedAt DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentAnalyticsForLevel(
        cefrLevel: String,
        limit: Int
    ): List<MissionAnalyticsWithObjectives>

    @Query(
        """
        SELECT COUNT(*)
        FROM mission_analytics
        WHERE cefrLevel = :cefrLevel
        """
    )
    suspend fun countMissionsForLevel(
        cefrLevel: String
    ): Int

    @Query(
        """
        DELETE FROM mission_analytics
        WHERE missionId = :missionId
        """
    )
    suspend fun deleteMissionAnalytics(
        missionId: String
    )
}