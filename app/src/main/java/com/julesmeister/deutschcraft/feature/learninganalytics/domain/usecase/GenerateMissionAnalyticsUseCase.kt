package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionAnalyticsRepository
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import javax.inject.Inject

/*
 * Generates and persists analytics for a completed mission.
 */
class GenerateMissionAnalyticsUseCase
@Inject constructor(
    private val buildMissionCompletionEvidence:
    BuildMissionCompletionEvidenceUseCase,
    private val calculateMissionAnalytics:
    CalculateMissionAnalyticsUseCase,
    private val analyticsRepository:
    MissionAnalyticsRepository
) {

    suspend operator fun invoke(
        mission:
        ConversationMission
    ): MissionAnalytics {
        require(
            mission.completedAt != null
        ) {
            "Mission analytics can only be finalized after " +
                    "the mission has completed."
        }

        val evidence =
            buildMissionCompletionEvidence(
                mission =
                    mission
            )

        val analytics =
            calculateMissionAnalytics(
                evidence =
                    evidence
            )

        analyticsRepository.save(
            analytics =
                analytics,
            evidence =
                evidence
        )

        return analytics
    }
}