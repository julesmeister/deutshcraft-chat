package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalyticsSummary
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class GetLatestConversationMissionAnalyticsUseCase
@Inject constructor(
    private val observeConversationMissionAnalytics:
    ObserveConversationMissionAnalyticsUseCase
) {

    suspend operator fun invoke(
        conversationId: String
    ): MissionAnalyticsSummary? {
        return observeConversationMissionAnalytics(
            conversationId =
                conversationId
        )
            .first()
            .firstOrNull()
    }
}