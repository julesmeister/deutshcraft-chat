// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/recommendation/MissionThreadRecommendationStore.kt

package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import kotlinx.coroutines.flow.Flow

interface MissionThreadRecommendationStore {

    fun observeNextEligible(
        cefrLevel:
        CefrLevel
    ): Flow<MissionThreadRecommendation?>

    suspend fun getNextEligible(
        cefrLevel:
        CefrLevel
    ): MissionThreadRecommendation?

    suspend fun getEligibleRecommendations(
        cefrLevel:
        CefrLevel,

        limit: Int
    ): List<MissionThreadRecommendation>

    suspend fun countEligible(
        cefrLevel:
        CefrLevel
    ): Int

    suspend fun getRecommendationHistory(
        cefrLevel:
        CefrLevel
    ): MissionRecommendationHistory

    suspend fun getRecentScenarioTypes(
        cefrLevel:
        CefrLevel
    ): Set<MissionScenarioType> {

        return getRecommendationHistory(
            cefrLevel =
                cefrLevel
        )
            .scenarioTypes
            .toSet()
    }

    suspend fun hasActiveScenario(
        cefrLevel:
        CefrLevel,

        scenarioType:
        MissionScenarioType
    ): Boolean

    suspend fun save(
        recommendation:
        MissionThreadRecommendation
    )

    suspend fun markDisplayed(
        recommendationId:
        String
    )

    suspend fun markSkipped(
        recommendationId:
        String
    )

    suspend fun markStale(
        recommendationId:
        String
    )

    suspend fun invalidateStaleRecommendations(
        cefrLevel:
        CefrLevel,

        currentLearningContext:
        MissionLearningContext
    ): Int

    suspend fun markStarted(
        recommendationId:
        String,

        conversationId:
        String,

        conversationMissionId:
        String
    )

    suspend fun markCompletedByMissionId(
        conversationMissionId:
        String
    )

    suspend fun performMaintenance(
        cefrLevel:
        CefrLevel
    )
}