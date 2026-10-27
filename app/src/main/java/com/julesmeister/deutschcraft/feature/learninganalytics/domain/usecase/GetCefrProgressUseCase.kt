package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.CefrProgressSnapshot
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionAnalyticsRepository
import javax.inject.Inject

/*
 * Loads persisted mission analytics and calculates the
 * learner's current CEFR progress snapshot.
 *
 * Current-level mastery and next-level readiness remain
 * separate.
 */
class GetCefrProgressUseCase
@Inject constructor(
    private val analyticsRepository:
    MissionAnalyticsRepository,
    private val calculateCefrProgress:
    CalculateCefrProgressUseCase
) {

    suspend operator fun invoke(
        currentLevel: CefrLevel
    ): CefrProgressSnapshot {
        val currentLevelAnalytics =
            analyticsRepository
                .getRecentForLevel(
                    cefrLevel =
                        currentLevel,
                    limit =
                        MAXIMUM_ANALYTICS_PER_LEVEL
                )

        val nextLevel =
            currentLevel.next()

        val nextLevelAnalytics =
            if (nextLevel == null) {
                emptyList()
            } else {
                analyticsRepository
                    .getRecentForLevel(
                        cefrLevel =
                            nextLevel,
                        limit =
                            MAXIMUM_ANALYTICS_PER_LEVEL
                    )
            }

        /*
         * CalculateCefrProgressUseCase performs its own level
         * filtering, but combining the two lists gives it both
         * current-level and direct next-level evidence.
         */
        val combinedAnalytics =
            (
                    currentLevelAnalytics +
                            nextLevelAnalytics
                    )
                .distinctBy { analytics ->
                    analytics.missionId
                }
                .sortedBy { analytics ->
                    analytics.missionId
                }

        return calculateCefrProgress(
            currentLevel =
                currentLevel,
            missionAnalytics =
                combinedAnalytics
        )
    }

    private companion object {
        const val MAXIMUM_ANALYTICS_PER_LEVEL =
            20
    }
}