package com.julesmeister.deutschcraft.core.activity

import androidx.room.withTransaction
import com.julesmeister.deutschcraft.core.activity.model.StreakSummary
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.ActivityDayDao
import com.julesmeister.deutschcraft.core.database.entity.ActivityDayEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class LocalActivityTracker @Inject constructor(
    private val database: DeutschCraftDatabase,
    private val activityDayDao: ActivityDayDao,
    private val clock: Clock
) : ActivityTracker {

    /*
     * Keeps existing callers working.
     *
     * A generic activity currently means one learner
     * message, so it delegates to recordMessageActivity().
     */
    override suspend fun recordActivity(
        profileId: String
    ): Int {
        return recordMessageActivity(
            profileId = profileId
        )
    }

    override suspend fun recordMessageActivity(
        profileId: String
    ): Int {
        return recordDailyActivity(
            profileId = profileId,
            messageIncrement = 1,
            assessmentIncrement = 0
        )
    }

    override suspend fun recordAssessmentActivity(
        profileId: String
    ): Int {
        return recordDailyActivity(
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

    /*
     * Keeps existing callers working when they only need
     * the current streak as an Int.
     */
    override suspend fun getStreakCount(
        profileId: String
    ): Int {
        return getStreak(
            profileId = profileId
        ).currentStreak
    }

    override fun observeStreak(
        profileId: String
    ): Flow<StreakSummary> {
        return activityDayDao
            .observeActiveEpochDays(
                profileId = profileId
            )
            .map { activeEpochDays ->
                calculateStreak(
                    activeEpochDays = activeEpochDays
                )
            }
    }

    override suspend fun getStreak(
        profileId: String
    ): StreakSummary {
        val activeEpochDays =
            activityDayDao.getActiveEpochDays(
                profileId = profileId
            )

        return calculateStreak(
            activeEpochDays = activeEpochDays
        )
    }

    private suspend fun recordDailyActivity(
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

    private fun calculateStreak(
        activeEpochDays: List<Long>
    ): StreakSummary {
        if (activeEpochDays.isEmpty()) {
            return StreakSummary.Empty
        }

        val sortedActiveDays = activeEpochDays
            .distinct()
            .sorted()

        val activeDaySet =
            sortedActiveDays.toHashSet()

        val todayEpochDay =
            currentLocalDate().toEpochDay()

        val yesterdayEpochDay =
            todayEpochDay - 1L

        val isActiveToday =
            todayEpochDay in activeDaySet

        val currentStreakEndDay =
            when {
                isActiveToday -> {
                    todayEpochDay
                }

                yesterdayEpochDay in activeDaySet -> {
                    yesterdayEpochDay
                }

                else -> {
                    null
                }
            }

        val currentStreak =
            if (currentStreakEndDay == null) {
                0
            } else {
                countBackwardStreak(
                    endEpochDay =
                        currentStreakEndDay,
                    activeDays =
                        activeDaySet
                )
            }

        val bestStreak =
            calculateBestStreak(
                sortedActiveDays =
                    sortedActiveDays
            )

        return StreakSummary(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            isActiveToday = isActiveToday,
            lastActiveEpochDay =
                sortedActiveDays.lastOrNull()
        )
    }

    private fun countBackwardStreak(
        endEpochDay: Long,
        activeDays: Set<Long>
    ): Int {
        var streakLength = 0
        var examinedEpochDay = endEpochDay

        while (examinedEpochDay in activeDays) {
            streakLength += 1
            examinedEpochDay -= 1L
        }

        return streakLength
    }

    private fun calculateBestStreak(
        sortedActiveDays: List<Long>
    ): Int {
        if (sortedActiveDays.isEmpty()) {
            return 0
        }

        var currentSequenceLength = 1
        var longestSequenceLength = 1

        for (
        index in 1 until sortedActiveDays.size
        ) {
            val previousEpochDay =
                sortedActiveDays[index - 1]

            val currentEpochDay =
                sortedActiveDays[index]

            if (
                currentEpochDay ==
                previousEpochDay + 1L
            ) {
                currentSequenceLength += 1
            } else {
                currentSequenceLength = 1
            }

            if (
                currentSequenceLength >
                longestSequenceLength
            ) {
                longestSequenceLength =
                    currentSequenceLength
            }
        }

        return longestSequenceLength
    }

    private fun currentLocalDate(): LocalDate {
        return clock
            .instant()
            .atZone(clock.zone)
            .toLocalDate()
    }
}