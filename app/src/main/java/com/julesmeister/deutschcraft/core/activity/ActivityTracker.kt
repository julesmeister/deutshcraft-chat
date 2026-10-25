package com.julesmeister.deutschcraft.core.activity

import com.julesmeister.deutschcraft.core.activity.model.StreakSummary
import kotlinx.coroutines.flow.Flow

interface ActivityTracker {

    /*
     * Compatibility method used by the existing
     * ChatViewModel or assessment pipeline.
     *
     * One call records one user-message activity.
     */
    suspend fun recordActivity(
        profileId: String
    ): Int

    suspend fun recordMessageActivity(
        profileId: String
    ): Int

    suspend fun recordAssessmentActivity(
        profileId: String
    ): Int

    suspend fun getActiveDayCount(
        profileId: String
    ): Int

    /*
     * Compatibility method for callers that only need
     * the current streak number.
     */
    suspend fun getStreakCount(
        profileId: String
    ): Int

    /*
     * Full streak information used by the Progress screen.
     */
    fun observeStreak(
        profileId: String
    ): Flow<StreakSummary>

    suspend fun getStreak(
        profileId: String
    ): StreakSummary
}