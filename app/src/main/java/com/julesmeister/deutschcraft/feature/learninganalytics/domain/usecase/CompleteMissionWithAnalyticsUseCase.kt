// app/src/main/java/com/julesmeister/deutschcraft/feature/learninganalytics/domain/usecase/CompleteMissionWithAnalyticsUseCase.kt

package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.mission.domain.MissionStatus
import com.julesmeister.deutschcraft.feature.mission.domain.repository.MissionRepository
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.LoadMissionThreadRecommendationUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.RefreshMissionRecommendationFreshnessUseCase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CompleteMissionWithAnalyticsUseCase
@Inject constructor(
    private val missionRepository:
    MissionRepository,

    private val finalizeCompletedMissionAnalytics:
    FinalizeCompletedMissionAnalyticsUseCase,

    private val loadMissionThreadRecommendation:
    LoadMissionThreadRecommendationUseCase,

    private val refreshMissionRecommendationFreshness:
    RefreshMissionRecommendationFreshnessUseCase
) {

    private val completionMutex =
        Mutex()

    suspend operator fun invoke(
        conversationMissionId:
        String
    ): MissionAnalytics {

        require(
            conversationMissionId.isNotBlank()
        ) {
            "Conversation mission ID must not be blank."
        }

        return completionMutex.withLock {
            try {
                val missionBeforeCompletion =
                    missionRepository
                        .getConversationMission(
                            conversationMissionId =
                                conversationMissionId
                        )
                        ?: throw IllegalStateException(
                            "Mission does not exist: " +
                                    conversationMissionId
                        )

                if (
                    missionBeforeCompletion.status !=
                    MissionStatus.COMPLETED
                ) {
                    missionRepository.completeMission(
                        conversationMissionId =
                            conversationMissionId
                    )
                }

                val analytics =
                    finalizeCompletedMissionAnalytics(
                        conversationMissionId =
                            conversationMissionId
                    )

                loadMissionThreadRecommendation.markCompleted(
                    conversationMissionId =
                        conversationMissionId
                )

                val invalidatedRecommendationCount =
                    refreshMissionRecommendationFreshness()

                Log.d(
                    USE_CASE_TAG,
                    buildString {
                        append(
                            "Mission completion finalized. "
                        )

                        append(
                            "missionId="
                        )

                        append(
                            conversationMissionId
                        )

                        append(
                            ", overall="
                        )

                        append(
                            analytics.overallScore
                        )

                        append(
                            ", invalidatedRecommendations="
                        )

                        append(
                            invalidatedRecommendationCount
                        )
                    }
                )

                analytics
            } catch (
                cancellationException:
                CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.e(
                    USE_CASE_TAG,
                    "Mission completion with analytics failed.",
                    throwable
                )

                throw throwable
            }
        }
    }

    private companion object {

        const val USE_CASE_TAG =
            "CompleteMissionAnalytics"
    }
}