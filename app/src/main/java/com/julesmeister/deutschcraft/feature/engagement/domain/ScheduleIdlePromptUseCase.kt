package com.julesmeister.deutschcraft.feature.engagement.domain

import com.julesmeister.deutschcraft.feature.engagement.data.EngagementPreferences
import com.julesmeister.deutschcraft.feature.engagement.notification.IdlePromptScheduler
import java.time.Clock
import javax.inject.Inject

class ScheduleIdlePromptUseCase @Inject constructor(
    private val preferences:
    EngagementPreferences,
    private val scheduler:
    IdlePromptScheduler,
    private val clock:
    Clock
) {

    operator fun invoke() {
        preferences.recordUserActivity(
            timestamp =
                clock.millis()
        )

        if (preferences.remindersEnabled) {
            scheduler.scheduleAfterLatestActivity()
        } else {
            scheduler.cancel()
        }
    }

    fun setEnabled(
        enabled: Boolean
    ) {
        preferences.remindersEnabled =
            enabled

        if (enabled) {
            preferences.recordUserActivity(
                timestamp =
                    clock.millis()
            )

            scheduler.scheduleAfterLatestActivity()
        } else {
            scheduler.cancel()
        }
    }
}