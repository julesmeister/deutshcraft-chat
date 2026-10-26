package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import javax.inject.Inject

class GetThreadMissionRecommendationUseCase
@Inject constructor(
    private val repository:
    ThreadMissionRecommendationRepository
) {

    suspend operator fun invoke(
        cefrLevel: CefrLevel,
        adaptiveLearningFocuses:
        List<String> =
            emptyList(),
        completedScenarioTypes:
        List<MissionScenarioType> =
            emptyList(),
        forceDifferentScenario: Boolean =
            false
    ): ThreadMissionRecommendation? {
        return repository.refreshRecommendation(
            cefrLevel =
                cefrLevel,
            adaptiveLearningFocuses =
                adaptiveLearningFocuses,
            completedScenarioTypes =
                completedScenarioTypes,
            forceDifferentScenario =
                forceDifferentScenario
        )
    }
}