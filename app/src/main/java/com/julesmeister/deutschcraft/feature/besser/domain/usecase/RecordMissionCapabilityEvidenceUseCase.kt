// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/domain/usecase/RecordMissionCapabilityEvidenceUseCase.kt

package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.mastery.MissionCapabilityEvidence
import com.julesmeister.deutschcraft.feature.besser.domain.repository.CapabilityRepository
import com.julesmeister.deutschcraft.feature.besser.domain.repository.MissionCapabilityEvidenceRepository
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import java.time.Clock
import javax.inject.Inject

class RecordMissionCapabilityEvidenceUseCase
@Inject constructor(
    private val capabilityRepository:
    CapabilityRepository,

    private val evidenceRepository:
    MissionCapabilityEvidenceRepository,

    private val clock:
    Clock
) {

    suspend operator fun invoke(
        mission:
        ConversationMission,

        analytics:
        MissionAnalytics
    ): List<MissionCapabilityEvidence> {

        require(
            mission.id ==
                    analytics.missionId
        ) {
            "Mission and analytics IDs must match."
        }

        require(
            mission.conversationId ==
                    analytics.conversationId
        ) {
            "Mission and analytics conversations must match."
        }

        val requestedCapabilityIds =
            mission
                .targetCapabilityIds
                .asSequence()
                .map { capabilityId ->
                    capabilityId.trim()
                }
                .filter { capabilityId ->
                    capabilityId.isNotBlank()
                }
                .distinct()
                .toList()

        if (requestedCapabilityIds.isEmpty()) {
            return emptyList()
        }

        /*
         * CapabilityRepository.getById() is suspend.
         *
         * Do not call it from Sequence.filter(), because that
         * lambda is not suspendable.
         */
        val validCapabilityIds =
            buildList {
                for (
                capabilityId in
                requestedCapabilityIds
                ) {
                    val capability =
                        capabilityRepository.getById(
                            capabilityId =
                                capabilityId
                        )

                    if (capability != null) {
                        add(
                            capabilityId
                        )
                    }
                }
            }

        if (validCapabilityIds.isEmpty()) {
            return emptyList()
        }

        val evidenceScore =
            calculateEvidenceScore(
                analytics =
                    analytics
            )

        val confidence =
            calculateConfidence(
                analytics =
                    analytics
            )

        val demonstrated =
            analytics
                .requiredObjectiveCompletionPercentage >=
                    MINIMUM_DEMONSTRATED_COMPLETION &&
                    evidenceScore >=
                    MINIMUM_DEMONSTRATED_SCORE

        val evaluatedAt =
            mission.completedAt
                ?: clock.millis()

        val evidence =
            validCapabilityIds.map { capabilityId ->
                MissionCapabilityEvidence(
                    missionId =
                        mission.id,

                    conversationId =
                        mission.conversationId,

                    capabilityId =
                        capabilityId,

                    overallScore =
                        analytics
                            .overallScore
                            .coercePercent(),

                    independenceScore =
                        analytics
                            .independenceScore
                            .coercePercent(),

                    objectiveCompletionPercentage =
                        analytics
                            .requiredObjectiveCompletionPercentage
                            .coercePercent(),

                    evidenceScore =
                        evidenceScore,

                    confidence =
                        confidence,

                    demonstrated =
                        demonstrated,

                    evaluatedAt =
                        evaluatedAt
                )
            }

        evidenceRepository.saveAll(
            evidence =
                evidence
        )

        return evidence
    }

    private fun calculateEvidenceScore(
        analytics:
        MissionAnalytics
    ): Int {

        val overallScore =
            analytics
                .overallScore
                .coercePercent()

        val independenceScore =
            analytics
                .independenceScore
                .coercePercent()

        val completionScore =
            analytics
                .requiredObjectiveCompletionPercentage
                .coercePercent()

        val interactionScore =
            analytics
                .interactionScore
                .coercePercent()

        val comprehensionScore =
            analytics
                .comprehensionScore
                .coercePercent()

        val communicationScore =
            (
                    interactionScore +
                            comprehensionScore
                    ) /
                    2f

        val weightedScore =
            overallScore.toFloat() *
                    OVERALL_SCORE_WEIGHT +
                    independenceScore.toFloat() *
                    INDEPENDENCE_SCORE_WEIGHT +
                    completionScore.toFloat() *
                    COMPLETION_SCORE_WEIGHT +
                    communicationScore *
                    COMMUNICATION_SCORE_WEIGHT

        return weightedScore
            .toInt()
            .coercePercent()
    }

    private fun calculateConfidence(
        analytics:
        MissionAnalytics
    ): Float {

        val objectiveCoverage =
            if (
                analytics.totalObjectiveCount <=
                0
            ) {
                0f
            } else {
                analytics.completedObjectiveCount
                    .toFloat()
                    .div(
                        analytics.totalObjectiveCount
                            .toFloat()
                    )
                    .coerceIn(
                        minimumValue =
                            0f,

                        maximumValue =
                            1f
                    )
            }

        val evidenceVolumeConfidence =
            analytics.totalObjectiveCount
                .toFloat()
                .div(
                    MINIMUM_FULL_CONFIDENCE_OBJECTIVE_COUNT
                        .toFloat()
                )
                .coerceIn(
                    minimumValue =
                        0f,

                    maximumValue =
                        1f
                )

        return (
                BASE_CONFIDENCE +
                        objectiveCoverage *
                        OBJECTIVE_COVERAGE_CONFIDENCE_WEIGHT +
                        evidenceVolumeConfidence *
                        EVIDENCE_VOLUME_CONFIDENCE_WEIGHT
                )
            .coerceIn(
                minimumValue =
                    MINIMUM_CONFIDENCE,

                maximumValue =
                    MAXIMUM_CONFIDENCE
            )
    }

    private fun Int.coercePercent():
            Int {

        return coerceIn(
            minimumValue =
                0,

            maximumValue =
                100
        )
    }

    private companion object {

        const val MINIMUM_DEMONSTRATED_COMPLETION =
            80

        const val MINIMUM_DEMONSTRATED_SCORE =
            65

        const val MINIMUM_FULL_CONFIDENCE_OBJECTIVE_COUNT =
            8

        const val OVERALL_SCORE_WEIGHT =
            0.35f

        const val INDEPENDENCE_SCORE_WEIGHT =
            0.25f

        const val COMPLETION_SCORE_WEIGHT =
            0.25f

        const val COMMUNICATION_SCORE_WEIGHT =
            0.15f

        const val BASE_CONFIDENCE =
            0.40f

        const val OBJECTIVE_COVERAGE_CONFIDENCE_WEIGHT =
            0.35f

        const val EVIDENCE_VOLUME_CONFIDENCE_WEIGHT =
            0.20f

        const val MINIMUM_CONFIDENCE =
            0.40f

        const val MAXIMUM_CONFIDENCE =
            0.95f
    }
}