package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import kotlinx.coroutines.flow.Flow

interface ThreadMissionRecommendationRepository {

    /*
     * Observes the recommendation currently displayed in the
     * top mission cell.
     */
    fun observeRecommendation():
            Flow<ThreadMissionRecommendation?>

    /*
     * Selects and publishes a recommendation suitable for
     * the learner's CEFR level.
     *
     * The same recently displayed scenario type is excluded.
     */
    suspend fun refreshRecommendation(
        cefrLevel: CefrLevel,
        adaptiveLearningFocuses: List<String> =
            emptyList(),
        completedScenarioTypes:
        List<MissionScenarioType> =
            emptyList(),
        forceDifferentScenario: Boolean =
            false
    ): ThreadMissionRecommendation?

    /*
     * Records that the learner saw this recommendation.
     *
     * This prevents semantically identical missions from
     * being displayed repeatedly under different titles.
     */
    suspend fun markRecommendationShown(
        scenarioType: MissionScenarioType
    )

    /*
     * Records that the learner dismissed the recommendation.
     */
    suspend fun dismissRecommendation(
        scenarioType: MissionScenarioType
    )

    /*
     * Clears the visible recommendation without erasing
     * scenario history.
     */
    suspend fun clearRecommendation()
}