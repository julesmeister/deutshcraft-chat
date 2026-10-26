package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveThreadMissionRecommendationUseCase
@Inject constructor(
    private val repository:
    ThreadMissionRecommendationRepository
) {

    operator fun invoke():
            Flow<ThreadMissionRecommendation?> {
        return repository.observeRecommendation()
    }
}