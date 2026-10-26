package com.julesmeister.deutschcraft.feature.threads.presentation

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionThreadRecommendationCache
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.LoadMissionThreadRecommendationUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.StartRecommendedMissionThreadUseCase
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import javax.inject.Inject

class ThreadMissionRecommendationCoordinator
@Inject constructor(
    private val loadRecommendation:
    LoadMissionThreadRecommendationUseCase,
    private val startRecommendedMission:
    StartRecommendedMissionThreadUseCase,
    private val recommendationCache:
    MissionThreadRecommendationCache
) {

    fun restoreCachedRecommendation():
            MissionThreadRecommendation? {
        return recommendationCache.get()
    }

    suspend fun loadRecommendation(
        activeThreads: List<ConversationThread>,
        forceRefresh: Boolean = false
    ): MissionThreadRecommendation? {
        val learnerLevel =
            determineLearnerLevel(
                activeThreads =
                    activeThreads
            )

        return loadRecommendation(
            cefrLevel =
                learnerLevel,
            forceRefresh =
                forceRefresh
        )
    }

    suspend fun startRecommendation(
        recommendation:
        MissionThreadRecommendation
    ): Conversation {
        return startRecommendedMission(
            recommendation =
                recommendation
        )
    }

    private fun determineLearnerLevel(
        activeThreads: List<ConversationThread>
    ): CefrLevel {
        /*
         * Prefer the level of the most recently updated
         * conversation.
         *
         * This gives the recommendation generator a useful
         * local level until a dedicated profile-level CEFR
         * source is connected to the thread screen.
         */
        return activeThreads
            .maxByOrNull { thread ->
                thread.updatedAt
            }
            ?.cefrLevel
            ?: CefrLevel.A1
    }
}