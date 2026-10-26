package com.julesmeister.deutschcraft.feature.engagement.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdlePromptScheduler @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val clock: Clock
) {

    fun scheduleAfterLatestActivity() {
        val delay =
            calculateInitialDelay()

        val request =
            OneTimeWorkRequestBuilder<
                    IdleConversationWorker
                    >()
                .setInitialDelay(
                    delay.toMillis(),
                    TimeUnit.MILLISECONDS
                )
                .addTag(IDLE_PROMPT_WORK_TAG)
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                UNIQUE_IDLE_PROMPT_WORK,
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    fun cancel() {
        WorkManager
            .getInstance(context)
            .cancelUniqueWork(
                UNIQUE_IDLE_PROMPT_WORK
            )
    }

    private fun calculateInitialDelay():
            Duration {
        val now =
            clock.instant()

        val intendedTime =
            now.plus(
                IDLE_DURATION
            )

        val intendedLocalDateTime =
            intendedTime.atZone(
                clock.zone
            )

        val intendedLocalTime =
            intendedLocalDateTime.toLocalTime()

        val adjustedInstant =
            if (
                intendedLocalTime >=
                QUIET_HOURS_START ||
                intendedLocalTime <
                QUIET_HOURS_END
            ) {
                val notificationDate =
                    if (
                        intendedLocalTime >=
                        QUIET_HOURS_START
                    ) {
                        intendedLocalDateTime
                            .toLocalDate()
                            .plusDays(1)
                    } else {
                        intendedLocalDateTime
                            .toLocalDate()
                    }

                notificationDate
                    .atTime(
                        QUIET_HOURS_END
                    )
                    .atZone(
                        clock.zone
                    )
                    .toInstant()
            } else {
                intendedTime
            }

        return Duration.between(
            now,
            adjustedInstant
        ).coerceAtLeast(
            Duration.ZERO
        )
    }

    companion object {
        const val UNIQUE_IDLE_PROMPT_WORK =
            "deutschcraft_idle_prompt"

        const val IDLE_PROMPT_WORK_TAG =
            "idle-conversation-prompt"

        val IDLE_DURATION: Duration =
            Duration.ofHours(3)

        val QUIET_HOURS_START: LocalTime =
            LocalTime.of(22, 0)

        val QUIET_HOURS_END: LocalTime =
            LocalTime.of(8, 0)
    }
}