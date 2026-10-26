package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import javax.inject.Inject

class DismissThreadMissionRecommendationUseCase
@Inject constructor(
    private val repository:
    ThreadMissionRecommendationRepository
) {

    suspend operator fun invoke(
        scenarioType: MissionScenarioType
    ) {
        repository.dismissRecommendation(
            scenarioType =
                scenarioType
        )
    }
}