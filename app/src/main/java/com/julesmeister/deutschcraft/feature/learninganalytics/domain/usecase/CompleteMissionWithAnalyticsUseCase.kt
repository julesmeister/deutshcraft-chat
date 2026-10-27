package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.mission.domain.MissionStatus
import com.julesmeister.deutschcraft.feature.mission.domain.repository.MissionRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*
 * Explicitly completes a mission and finalizes its analytics.
 *
 * Use this when code intentionally completes the complete
 * mission rather than completing an individual objective.
 */
class CompleteMissionWithAnalyticsUseCase
@Inject constructor(
    private val missionRepository:
    MissionRepository,
    private val finalizeCompletedMissionAnalytics:
    FinalizeCompletedMissionAnalyticsUseCase
) {

    private val completionMutex =
        Mutex()

    suspend operator fun invoke(
        conversationMissionId: String
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

                /*
                 * Avoid rewriting the completion timestamp when
                 * the mission is already complete.
                 */
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

                Log.d(
                    USE_CASE_TAG,
                    buildString {
                        append(
                            "Explicit mission completion "
                        )
                        append(
                            "analytics finalized. missionId="
                        )
                        append(
                            conversationMissionId
                        )
                        append(", overall=")
                        append(
                            analytics.overallScore
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