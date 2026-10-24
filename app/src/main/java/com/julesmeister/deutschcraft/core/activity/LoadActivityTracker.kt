package com.julesmeister.deutschcraft.core.activity

import androidx.room.withTransaction
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.ActivityDayDao
import com.julesmeister.deutschcraft.core.database.entity.ActivityDayEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalActivityTracker @Inject constructor(
    private val database: DeutschCraftDatabase,
    private val activityDayDao: ActivityDayDao,
    private val clock: Clock
) : ActivityTracker {

    override suspend fun recordMessageActivity(
        profileId: String
    ): Int {
        return recordActivity(
            profileId = profileId,
            messageIncrement = 1,
            assessmentIncrement = 0
        )
    }

    override suspend fun recordAssessmentActivity(
        profileId: String
    ): Int {
        return recordActivity(
            profileId = profileId,
            messageIncrement = 0,
            assessmentIncrement = 1
        )
    }

    override suspend fun getActiveDayCount(
        profileId: String
    ): Int {
        return activityDayDao.countActiveDays(
            profileId = profileId
        )
    }

    private suspend fun recordActivity(
        profileId: String,
        messageIncrement: Int,
        assessmentIncrement: Int
    ): Int {
        val now = clock.instant()
        val zoneId = clock.zone

        val localDate = now
            .atZone(zoneId)
            .toLocalDate()

        val epochDay = localDate.toEpochDay()
        val timestamp = now.toEpochMilli()

        return database.withTransaction {
            val existingDay =
                activityDayDao.getActivityDay(
                    profileId = profileId,
                    epochDay = epochDay
                )

            val updatedDay =
                if (existingDay == null) {
                    createActivityDay(
                        profileId = profileId,
                        localDate = localDate,
                        zoneId = zoneId,
                        timestamp = timestamp,
                        messageIncrement =
                            messageIncrement,
                        assessmentIncrement =
                            assessmentIncrement
                    )
                } else {
                    existingDay.copy(
                        messageCount =
                            existingDay.messageCount +
                                    messageIncrement,
                        assessmentCount =
                            existingDay.assessmentCount +
                                    assessmentIncrement,
                        lastActivityAt = timestamp,
                        syncStatus =
                            SyncStatusEntity.LOCAL_ONLY
                    )
                }

            activityDayDao.upsertActivityDay(
                updatedDay
            )

            activityDayDao.countActiveDays(
                profileId = profileId
            )
        }
    }

    private fun createActivityDay(
        profileId: String,
        localDate: LocalDate,
        zoneId: ZoneId,
        timestamp: Long,
        messageIncrement: Int,
        assessmentIncrement: Int
    ): ActivityDayEntity {
        return ActivityDayEntity(
            profileId = profileId,
            epochDay = localDate.toEpochDay(),
            localDate = localDate.toString(),
            zoneId = zoneId.id,
            messageCount = messageIncrement,
            assessmentCount =
                assessmentIncrement,
            firstActivityAt = timestamp,
            lastActivityAt = timestamp,
            syncStatus =
                SyncStatusEntity.LOCAL_ONLY,
            remoteVersion = null,
            lastSyncedAt = null
        )
    }
}