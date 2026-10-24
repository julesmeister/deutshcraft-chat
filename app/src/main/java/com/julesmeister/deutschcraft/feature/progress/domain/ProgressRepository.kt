package com.julesmeister.deutschcraft.feature.progress.domain

import kotlinx.coroutines.flow.Flow

interface ProgressRepository {

    fun observeProgress():
            Flow<ProgressSummary?>

    suspend fun getProgress():
            ProgressSummary?

    suspend fun ensureProfileExists(): Unit
}