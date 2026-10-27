package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionAnalyticsRepository
import com.julesmeister.deutschcraft.feature.mission.domain.MissionStatus
import com.julesmeister.deutschcraft.feature.mission.domain.repository.MissionRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*
 * Finalizes analytics for one completed ConversationMission.
 *
 * This operation is idempotent:
 *
 * - If analytics already exist, return them.
 * - Otherwise load the completed mission, calculate the
 *   analytics, and persist the result.
 */
class FinalizeCompletedMissionAnalyticsUseCase
@Inject constructor(
    private val missionRepository:
    MissionRepository,
    private val analyticsRepository:
    MissionAnalyticsRepository,
    private val generateMissionAnalytics:
    GenerateMissionAnalyticsUseCase
) {

    private val finalizationMutex =
        Mutex()

    suspend operator fun invoke(
        conversationMissionId: String
    ): MissionAnalytics {
        require(
            conversationMissionId.isNotBlank()
        ) {
            "Conversation mission ID must not be blank."
        }

        return finalizationMutex.withLock {
            val existingAnalytics =
                analyticsRepository.getByMissionId(
                    missionId =
                        conversationMissionId
                )

            if (existingAnalytics != null) {
                return@withLock existingAnalytics
            }

            val mission =
                missionRepository
                    .getConversationMission(
                        conversationMissionId =
                            conversationMissionId
                    )
                    ?: throw IllegalStateException(
                        "Conversation mission does not exist: " +
                                conversationMissionId
                    )

            check(
                mission.status ==
                        MissionStatus.COMPLETED
            ) {
                "Mission analytics can only be finalized for " +
                        "a completed mission."
            }

            check(
                mission.completedAt !=
                        null
            ) {
                "Completed mission has no completion time."
            }

            try {
                val analytics =
                    generateMissionAnalytics(
                        mission =
                            mission
                    )

                Log.d(
                    USE_CASE_TAG,
                    buildString {
                        append("Finalized mission analytics. ")
                        append("missionId=")
                        append(conversationMissionId)
                        append(", overall=")
                        append(analytics.overallScore)
                        append(", independence=")
                        append(analytics.independenceScore)
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
                    "Mission analytics could not be finalized.",
                    throwable
                )

                throw throwable
            }
        }
    }

    private companion object {
        const val USE_CASE_TAG =
            "FinalizeMissionAnalytics"
    }
}