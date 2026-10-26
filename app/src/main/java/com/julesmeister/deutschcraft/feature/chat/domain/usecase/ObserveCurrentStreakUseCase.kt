package com.julesmeister.deutschcraft.feature.chat.domain.usecase

import com.julesmeister.deutschcraft.feature.progress.domain.ProgressRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveCurrentStreakUseCase @Inject constructor(
    private val progressRepository:
    ProgressRepository
) {

    operator fun invoke(): Flow<Int> {
        return progressRepository
            .observeProgress()
            .map { progressSummary ->
                progressSummary
                    ?.activeDayCount
                    ?.coerceAtLeast(0)
                    ?: 0
            }
    }
}