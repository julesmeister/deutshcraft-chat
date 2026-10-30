// app/src/main/java/com/julesmeister/deutschcraft/feature/adaptivelearning/domain/usecase/RecordMissionAdaptiveLearningEvidenceUseCase.kt

package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningObservation
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningRecordResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.LearningEvidenceResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.repository.AdaptiveLearningRepository
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class RecordMissionAdaptiveLearningEvidenceUseCase
@Inject constructor(
    private val adaptiveLearningRepository:
    AdaptiveLearningRepository,

    private val recordAdaptiveLearningEvidence:
    RecordAdaptiveLearningEvidenceUseCase
) {

    suspend operator fun invoke(
        mission:
        ConversationMission,

        analytics:
        MissionAnalytics
    ): List<AdaptiveLearningRecordResult> {

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

        val adaptiveFocusKeys =
            mission
                .adaptiveFocusKeys
                .asSequence()
                .map(
                    ::normalizeFocusKey
                )
                .filter(
                    String::isNotBlank
                )
                .distinct()
                .toList()

        if (adaptiveFocusKeys.isEmpty()) {
            return emptyList()
        }

        val evidenceResult =
            determineEvidenceResult(
                analytics =
                    analytics
            )

        val evidenceConfidence =
            calculateEvidenceConfidence(
                analytics =
                    analytics
            )

        val evidenceScore =
            calculateEvidenceScore(
                analytics =
                    analytics
            )

        val missionEvidenceId =
            createMissionEvidenceId(
                missionId =
                    mission.id
            )

        val learnerText =
            createMissionEvidenceText(
                mission =
                    mission,

                analytics =
                    analytics,

                evidenceScore =
                    evidenceScore
            )

        val results =
            mutableListOf<AdaptiveLearningRecordResult>()

        for (
        focusKey in
        adaptiveFocusKeys
        ) {
            try {
                val existingFocus =
                    adaptiveLearningRepository
                        .getFocusByKey(
                            focusKey =
                                focusKey
                        )
                        ?: run {
                            Log.w(
                                USE_CASE_TAG,
                                buildString {
                                    append(
                                        "Mission adaptive evidence skipped "
                                    )

                                    append(
                                        "because the focus was not found. "
                                    )

                                    append(
                                        "missionId="
                                    )

                                    append(
                                        mission.id
                                    )

                                    append(
                                        ", focusKey="
                                    )

                                    append(
                                        focusKey
                                    )
                                }
                            )

                            results.add(
                                AdaptiveLearningRecordResult.Skipped(
                                    reason =
                                        "Adaptive-learning focus was not found: " +
                                                focusKey
                                )
                            )

                            continue
                        }

                val observation =
                    AdaptiveLearningObservation(
                        focusKey =
                            existingFocus.focusKey,

                        type =
                            existingFocus.type,

                        title =
                            existingFocus.title,

                        description =
                            existingFocus.description,

                        targetCefrLevel =
                            existingFocus.targetCefrLevel,

                        communicationSkill =
                            existingFocus.communicationSkill,

                        result =
                            evidenceResult,

                        confidence =
                            evidenceConfidence,

                        reason =
                            createEvidenceReason(
                                mission =
                                    mission,

                                analytics =
                                    analytics,

                                evidenceScore =
                                    evidenceScore,

                                result =
                                    evidenceResult
                            ),

                        reinforcementInstruction =
                            existingFocus
                                .reinforcementInstruction,

                        learnerGuidance =
                            existingFocus.learnerGuidance,

                        learnerText =
                            learnerText,

                        correctedText =
                            null,

                        levelAppropriateText =
                            null
                    )

                val result =
                    recordAdaptiveLearningEvidence(
                        messageId =
                            missionEvidenceId,

                        conversationId =
                            mission.conversationId,

                        observation =
                            observation
                    )

                results.add(
                    result
                )

                Log.d(
                    USE_CASE_TAG,
                    buildString {
                        append(
                            "Mission adaptive evidence processed. "
                        )

                        append(
                            "missionId="
                        )

                        append(
                            mission.id
                        )

                        append(
                            ", focusKey="
                        )

                        append(
                            focusKey
                        )

                        append(
                            ", result="
                        )

                        append(
                            evidenceResult
                        )

                        append(
                            ", evidenceScore="
                        )

                        append(
                            evidenceScore
                        )

                        append(
                            ", confidence="
                        )

                        append(
                            evidenceConfidence
                        )

                        append(
                            ", recordResult="
                        )

                        append(
                            result::class.simpleName
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
                    buildString {
                        append(
                            "Mission adaptive evidence failed. "
                        )

                        append(
                            "missionId="
                        )

                        append(
                            mission.id
                        )

                        append(
                            ", focusKey="
                        )

                        append(
                            focusKey
                        )
                    },
                    throwable
                )

                results.add(
                    AdaptiveLearningRecordResult.Failed(
                        message =
                            throwable.message
                                ?: DEFAULT_RECORDING_ERROR
                    )
                )
            }
        }

        return results
    }

    private fun determineEvidenceResult(
        analytics:
        MissionAnalytics
    ): LearningEvidenceResult {

        val evidenceScore =
            calculateEvidenceScore(
                analytics =
                    analytics
            )

        val requiredCompletion =
            analytics
                .requiredObjectiveCompletionPercentage
                .coercePercent()

        return when {
            evidenceScore >=
                    SUCCESS_SCORE_THRESHOLD &&
                    requiredCompletion >=
                    SUCCESS_COMPLETION_THRESHOLD &&
                    analytics.independenceScore >=
                    SUCCESS_INDEPENDENCE_THRESHOLD -> {

                LearningEvidenceResult.SUCCESS
            }

            evidenceScore >=
                    PARTIAL_SUCCESS_SCORE_THRESHOLD &&
                    requiredCompletion >=
                    PARTIAL_SUCCESS_COMPLETION_THRESHOLD -> {

                LearningEvidenceResult.PARTIAL_SUCCESS
            }

            else -> {
                LearningEvidenceResult.STRUGGLED
            }
        }
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

        val requiredCompletionScore =
            analytics
                .requiredObjectiveCompletionPercentage
                .coercePercent()

        val communicationScore =
            (
                    analytics
                        .interactionScore
                        .coercePercent() +
                            analytics
                                .comprehensionScore
                                .coercePercent()
                    ) /
                    2f

        val weightedScore =
            overallScore.toFloat() *
                    OVERALL_SCORE_WEIGHT +
                    independenceScore.toFloat() *
                    INDEPENDENCE_SCORE_WEIGHT +
                    requiredCompletionScore.toFloat() *
                    COMPLETION_SCORE_WEIGHT +
                    communicationScore *
                    COMMUNICATION_SCORE_WEIGHT

        return weightedScore
            .toInt()
            .coercePercent()
    }

    private fun calculateEvidenceConfidence(
        analytics:
        MissionAnalytics
    ): Float {

        val totalObjectiveCount =
            analytics
                .totalObjectiveCount
                .coerceAtLeast(
                    minimumValue =
                        0
                )

        val completedObjectiveCount =
            analytics
                .completedObjectiveCount
                .coerceIn(
                    minimumValue =
                        0,

                    maximumValue =
                        totalObjectiveCount
                            .coerceAtLeast(
                                minimumValue =
                                    0
                            )
                )

        val objectiveCoverage =
            if (totalObjectiveCount <= 0) {
                0f
            } else {
                completedObjectiveCount
                    .toFloat()
                    .div(
                        totalObjectiveCount
                            .toFloat()
                    )
                    .coerceIn(
                        minimumValue =
                            0f,

                        maximumValue =
                            1f
                    )
            }

        val evidenceVolume =
            totalObjectiveCount
                .toFloat()
                .div(
                    FULL_CONFIDENCE_OBJECTIVE_COUNT
                        .toFloat()
                )
                .coerceIn(
                    minimumValue =
                        0f,

                    maximumValue =
                        1f
                )

        val requiredCompletionConfidence =
            analytics
                .requiredObjectiveCompletionPercentage
                .coercePercent()
                .toFloat()
                .div(
                    100f
                )

        return (
                BASE_CONFIDENCE +
                        objectiveCoverage *
                        OBJECTIVE_COVERAGE_WEIGHT +
                        evidenceVolume *
                        EVIDENCE_VOLUME_WEIGHT +
                        requiredCompletionConfidence *
                        REQUIRED_COMPLETION_WEIGHT
                )
            .coerceIn(
                minimumValue =
                    MINIMUM_EVIDENCE_CONFIDENCE,

                maximumValue =
                    MAXIMUM_EVIDENCE_CONFIDENCE
            )
    }

    private fun createEvidenceReason(
        mission:
        ConversationMission,

        analytics:
        MissionAnalytics,

        evidenceScore: Int,

        result:
        LearningEvidenceResult
    ): String {

        return buildString {
            append(
                "Targeted mission evidence from "
            )

            append(
                mission.template.title.trim()
            )

            append(
                ". Result="
            )

            append(
                result.name
            )

            append(
                ", evidenceScore="
            )

            append(
                evidenceScore
            )

            append(
                ", overallScore="
            )

            append(
                analytics
                    .overallScore
                    .coercePercent()
            )

            append(
                ", independenceScore="
            )

            append(
                analytics
                    .independenceScore
                    .coercePercent()
            )

            append(
                ", requiredObjectiveCompletion="
            )

            append(
                analytics
                    .requiredObjectiveCompletionPercentage
                    .coercePercent()
            )

            append(
                ", interactionScore="
            )

            append(
                analytics
                    .interactionScore
                    .coercePercent()
            )

            append(
                ", comprehensionScore="
            )

            append(
                analytics
                    .comprehensionScore
                    .coercePercent()
            )

            append(
                "."
            )
        }
    }

    private fun createMissionEvidenceText(
        mission:
        ConversationMission,

        analytics:
        MissionAnalytics,

        evidenceScore: Int
    ): String {

        return buildString {
            append(
                "Mission: "
            )

            append(
                mission
                    .template
                    .title
                    .trim()
                    .ifBlank {
                        DEFAULT_MISSION_TITLE
                    }
            )

            append(
                ". Required objectives: "
            )

            append(
                analytics
                    .completedRequiredObjectiveCount
                    .coerceAtLeast(
                        minimumValue =
                            0
                    )
            )

            append(
                "/"
            )

            append(
                analytics
                    .totalRequiredObjectiveCount
                    .coerceAtLeast(
                        minimumValue =
                            0
                    )
            )

            append(
                ". Evidence score: "
            )

            append(
                evidenceScore
            )

            append(
                "."
            )
        }
    }

    private fun createMissionEvidenceId(
        missionId: String
    ): String {

        return MISSION_EVIDENCE_ID_PREFIX +
                missionId.trim()
    }

    private fun normalizeFocusKey(
        value: String
    ): String {

        return value
            .trim()
            .lowercase()
            .replace(
                WHITESPACE_REGEX,
                "_"
            )
            .replace(
                INVALID_KEY_CHARACTER_REGEX,
                ""
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

        const val USE_CASE_TAG =
            "MissionAdaptiveEvidence"

        const val DEFAULT_RECORDING_ERROR =
            "Mission adaptive-learning evidence could not be saved."

        const val DEFAULT_MISSION_TITLE =
            "Completed mission"

        const val MISSION_EVIDENCE_ID_PREFIX =
            "mission_evidence:"

        const val SUCCESS_SCORE_THRESHOLD =
            75

        const val SUCCESS_COMPLETION_THRESHOLD =
            90

        const val SUCCESS_INDEPENDENCE_THRESHOLD =
            65

        const val PARTIAL_SUCCESS_SCORE_THRESHOLD =
            55

        const val PARTIAL_SUCCESS_COMPLETION_THRESHOLD =
            60

        const val FULL_CONFIDENCE_OBJECTIVE_COUNT =
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
            0.35f

        const val OBJECTIVE_COVERAGE_WEIGHT =
            0.25f

        const val EVIDENCE_VOLUME_WEIGHT =
            0.20f

        const val REQUIRED_COMPLETION_WEIGHT =
            0.15f

        const val MINIMUM_EVIDENCE_CONFIDENCE =
            0.40f

        const val MAXIMUM_EVIDENCE_CONFIDENCE =
            0.95f

        val WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )

        val INVALID_KEY_CHARACTER_REGEX =
            Regex(
                "[^a-z0-9:_-]"
            )
    }
}