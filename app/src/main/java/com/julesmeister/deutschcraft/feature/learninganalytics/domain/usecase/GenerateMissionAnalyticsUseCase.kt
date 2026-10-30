// app/src/main/java/com/julesmeister/deutschcraft/feature/learninganalytics/domain/usecase/GenerateMissionAnalyticsUseCase.kt

package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningRecordResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.usecase.RecordMissionAdaptiveLearningEvidenceUseCase
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.RecordMissionCapabilityEvidenceUseCase
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionAnalyticsRepository
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class GenerateMissionAnalyticsUseCase
@Inject constructor(
    private val buildMissionCompletionEvidence:
    BuildMissionCompletionEvidenceUseCase,

    private val calculateMissionAnalytics:
    CalculateMissionAnalyticsUseCase,

    private val analyticsRepository:
    MissionAnalyticsRepository,

    private val recordMissionCapabilityEvidence:
    RecordMissionCapabilityEvidenceUseCase,

    private val recordMissionAdaptiveLearningEvidence:
    RecordMissionAdaptiveLearningEvidenceUseCase
) {

    suspend operator fun invoke(
        mission:
        ConversationMission
    ): MissionAnalytics {

        require(
            mission.completedAt !=
                    null
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

        recordCapabilityEvidenceSafely(
            mission =
                mission,

            analytics =
                analytics
        )

        recordAdaptiveLearningEvidenceSafely(
            mission =
                mission,

            analytics =
                analytics
        )

        return analytics
    }

    private suspend fun recordCapabilityEvidenceSafely(
        mission:
        ConversationMission,

        analytics:
        MissionAnalytics
    ) {
        if (mission.targetCapabilityIds.isEmpty()) {
            return
        }

        try {
            val capabilityEvidence =
                recordMissionCapabilityEvidence(
                    mission =
                        mission,

                    analytics =
                        analytics
                )

            Log.d(
                USE_CASE_TAG,
                buildString {
                    append(
                        "Targeted mission capability evidence "
                    )

                    append(
                        "processed. missionId="
                    )

                    append(
                        mission.id
                    )

                    append(
                        ", requestedCapabilities="
                    )

                    append(
                        mission.targetCapabilityIds.size
                    )

                    append(
                        ", recordedEvidence="
                    )

                    append(
                        capabilityEvidence.size
                    )
                }
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                USE_CASE_TAG,
                "Mission analytics were saved, but capability " +
                        "evidence could not be recorded.",
                throwable
            )
        }
    }

    private suspend fun recordAdaptiveLearningEvidenceSafely(
        mission:
        ConversationMission,

        analytics:
        MissionAnalytics
    ) {
        if (mission.adaptiveFocusKeys.isEmpty()) {
            return
        }

        try {
            val adaptiveResults =
                recordMissionAdaptiveLearningEvidence(
                    mission =
                        mission,

                    analytics =
                        analytics
                )

            val recordedCount =
                adaptiveResults.count { result ->
                    result is
                            AdaptiveLearningRecordResult.Recorded
                }

            val skippedCount =
                adaptiveResults.count { result ->
                    result is
                            AdaptiveLearningRecordResult.Skipped
                }

            val failedCount =
                adaptiveResults.count { result ->
                    result is
                            AdaptiveLearningRecordResult.Failed
                }

            Log.d(
                USE_CASE_TAG,
                buildString {
                    append(
                        "Targeted mission adaptive evidence "
                    )

                    append(
                        "processed. missionId="
                    )

                    append(
                        mission.id
                    )

                    append(
                        ", requestedFocuses="
                    )

                    append(
                        mission.adaptiveFocusKeys.size
                    )

                    append(
                        ", recorded="
                    )

                    append(
                        recordedCount
                    )

                    append(
                        ", skipped="
                    )

                    append(
                        skippedCount
                    )

                    append(
                        ", failed="
                    )

                    append(
                        failedCount
                    )
                }
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                USE_CASE_TAG,
                "Mission analytics were saved, but adaptive " +
                        "learning evidence could not be recorded.",
                throwable
            )
        }
    }

    private companion object {

        const val USE_CASE_TAG =
            "GenerateMissionAnalytics"
    }
}