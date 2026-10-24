package com.julesmeister.deutschcraft.core.activity

interface ActivityTracker {

    /**
     * Records one sent user message for today's
     * local calendar date.
     *
     * Returns the learner's total active-day count.
     */
    suspend fun recordMessageActivity(
        profileId: String
    ): Int

    /**
     * Records one completed assessment for today's
     * local calendar date.
     *
     * Returns the learner's total active-day count.
     */
    suspend fun recordAssessmentActivity(
        profileId: String
    ): Int

    suspend fun getActiveDayCount(
        profileId: String
    ): Int
}