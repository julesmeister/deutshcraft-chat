package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.julesmeister.deutschcraft.core.database.entity.ActivityDayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDayDao {

    @Upsert
    suspend fun upsertActivityDay(
        activityDay: ActivityDayEntity
    )

    @Query(
        """
        SELECT *
        FROM activity_days
        WHERE profileId = :profileId
          AND epochDay = :epochDay
        LIMIT 1
        """
    )
    suspend fun getActivityDay(
        profileId: String,
        epochDay: Long
    ): ActivityDayEntity?

    @Query(
        """
        SELECT *
        FROM activity_days
        WHERE profileId = :profileId
        ORDER BY epochDay DESC
        """
    )
    fun observeActivityDays(
        profileId: String
    ): Flow<List<ActivityDayEntity>>

    @Query(
        """
    SELECT epochDay
    FROM activity_days
    WHERE profileId = :profileId
    ORDER BY epochDay ASC
    """
    )
    fun observeActiveEpochDays(
        profileId: String
    ): Flow<List<Long>>

    @Query(
        """
    SELECT epochDay
    FROM activity_days
    WHERE profileId = :profileId
    ORDER BY epochDay ASC
    """
    )
    suspend fun getActiveEpochDays(
        profileId: String
    ): List<Long>


    @Query(
        """
        SELECT *
        FROM activity_days
        WHERE profileId = :profileId
        ORDER BY epochDay DESC
        """
    )
    suspend fun getActivityDays(
        profileId: String
    ): List<ActivityDayEntity>

    @Query(
        """
        SELECT COUNT(*)
        FROM activity_days
        WHERE profileId = :profileId
        """
    )
    suspend fun countActiveDays(
        profileId: String
    ): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM activity_days
        WHERE profileId = :profileId
          AND epochDay >= :startEpochDay
          AND epochDay <= :endEpochDay
        """
    )
    suspend fun countActiveDaysBetween(
        profileId: String,
        startEpochDay: Long,
        endEpochDay: Long
    ): Int

    @Query(
        """
        SELECT MAX(epochDay)
        FROM activity_days
        WHERE profileId = :profileId
        """
    )
    suspend fun getLatestActiveEpochDay(
        profileId: String
    ): Long?

    @Query(
        """
        DELETE FROM activity_days
        WHERE profileId = :profileId
        """
    )
    suspend fun deleteActivityDays(
        profileId: String
    )
}