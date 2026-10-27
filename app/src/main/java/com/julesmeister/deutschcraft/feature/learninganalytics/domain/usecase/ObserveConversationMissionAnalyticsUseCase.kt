package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalyticsSummary
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionAnalyticsRepository
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.toSummary
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
 * Observes finalized mission analytics for one conversation.
 *
 * The latest item can be used for a completion dialog or
 * mission-results card.
 */
class ObserveConversationMissionAnalyticsUseCase
@Inject constructor(
    private val repository:
    MissionAnalyticsRepository
) {

    operator fun invoke(
        conversationId: String
    ): Flow<List<MissionAnalyticsSummary>> {
        require(
            conversationId.isNotBlank()
        ) {
            "Conversation ID must not be blank."
        }

        return repository
            .observeConversationAnalytics(
                conversationId =
                    conversationId
            )
            .map { analytics ->
                analytics.map { missionAnalytics ->
                    missionAnalytics.toSummary()
                }
            }
    }
}