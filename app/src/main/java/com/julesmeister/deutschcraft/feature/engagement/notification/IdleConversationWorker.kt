package com.julesmeister.deutschcraft.feature.engagement.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.julesmeister.deutschcraft.feature.engagement.data.EngagementPreferences
import com.julesmeister.deutschcraft.feature.engagement.data.LocalConversationStarterSource
import java.time.Clock
import java.time.Duration
import java.time.LocalTime

class IdleConversationWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParameters
) {

    override suspend fun doWork():
            Result {
        val clock =
            Clock.systemDefaultZone()

        val preferences =
            EngagementPreferences(
                context =
                    applicationContext
            )

        if (!preferences.remindersEnabled) {
            return Result.success()
        }

        val lastActivityAt =
            preferences.lastUserActivityAt

        if (lastActivityAt <= 0L) {
            return Result.success()
        }

        val now =
            clock.instant()

        val inactiveDuration =
            Duration.between(
                java.time.Instant.ofEpochMilli(
                    lastActivityAt
                ),
                now
            )

        if (
            inactiveDuration <
            IdlePromptScheduler.IDLE_DURATION
        ) {
            return Result.success()
        }

        val localDateTime =
            now.atZone(
                clock.zone
            )

        if (
            isQuietTime(
                localTime =
                    localDateTime.toLocalTime()
            )
        ) {
            return Result.success()
        }

        val currentEpochDay =
            localDateTime
                .toLocalDate()
                .toEpochDay()

        if (
            preferences.lastNotificationEpochDay ==
            currentEpochDay
        ) {
            return Result.success()
        }

        val notificationManager =
            DeutschCraftNotificationManager(
                context =
                    applicationContext
            )

        if (!notificationManager.canPostNotifications()) {
            return Result.success()
        }

        val starterSource =
            LocalConversationStarterSource()

        val starter =
            starterSource.selectStarter(
                excludedStarterId =
                    preferences.lastStarterId
            )

        val posted =
            notificationManager
                .showConversationStarter(
                    starter =
                        starter
                )

        if (posted) {
            preferences.recordNotification(
                epochDay =
                    currentEpochDay,
                starterId =
                    starter.id
            )
        }

        return Result.success()
    }

    private fun isQuietTime(
        localTime: LocalTime
    ): Boolean {
        return localTime >=
                IdlePromptScheduler
                    .QUIET_HOURS_START ||
                localTime <
                IdlePromptScheduler
                    .QUIET_HOURS_END
    }
}