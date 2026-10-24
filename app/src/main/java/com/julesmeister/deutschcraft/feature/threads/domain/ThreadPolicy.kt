package com.julesmeister.deutschcraft.feature.threads.domain

import java.time.Clock
import javax.inject.Inject

class ThreadPolicy @Inject constructor(
    private val clock: Clock
) {

    fun shouldCreateNewThread(
        latestThreadEpochDay: Long?
    ): Boolean {
        if (latestThreadEpochDay == null) {
            return true
        }

        val currentEpochDay = clock
            .instant()
            .atZone(clock.zone)
            .toLocalDate()
            .toEpochDay()

        return latestThreadEpochDay !=
                currentEpochDay
    }
}