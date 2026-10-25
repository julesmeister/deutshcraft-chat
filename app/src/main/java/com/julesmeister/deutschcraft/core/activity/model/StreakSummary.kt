package com.julesmeister.deutschcraft.core.activity.model

data class StreakSummary(
    val currentStreak: Int,
    val bestStreak: Int,
    val isActiveToday: Boolean,
    val lastActiveEpochDay: Long?
) {
    companion object {
        val Empty = StreakSummary(
            currentStreak = 0,
            bestStreak = 0,
            isActiveToday = false,
            lastActiveEpochDay = null
        )
    }
}