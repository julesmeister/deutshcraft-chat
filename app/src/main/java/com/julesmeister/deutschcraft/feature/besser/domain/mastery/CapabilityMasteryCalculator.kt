// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/domain/mastery/CapabilityMasteryCalculator.kt

package com.julesmeister.deutschcraft.feature.besser.domain.mastery

import com.julesmeister.deutschcraft.feature.besser.domain.BesserChallenge
import com.julesmeister.deutschcraft.feature.besser.domain.ChallengeStatus
import javax.inject.Inject

class CapabilityMasteryCalculator
@Inject constructor() {

    fun calculate(
        capabilityId: String,

        challenges:
        List<BesserChallenge>,

        missionEvidence:
        List<MissionCapabilityEvidence> =
            emptyList()
    ): CapabilityMastery {

        require(
            capabilityId.isNotBlank()
        ) {
            "Capability ID cannot be blank."
        }

        val challengeEvidence =
            challenges
                .asSequence()
                .filter { challenge ->
                    challenge.capabilityId ==
                            capabilityId
                }
                .filter { challenge ->
                    challenge.status ==
                            ChallengeStatus.COMPLETED ||
                            challenge.status ==
                            ChallengeStatus.SKIPPED
                }
                .map { challenge ->
                    MasteryEvidenceItem(
                        value =
                            challenge.evidenceValue(),

                        timestamp =
                            challenge.terminalTimestamp(),

                        completed =
                            challenge.status ==
                                    ChallengeStatus.COMPLETED,

                        skipped =
                            challenge.status ==
                                    ChallengeStatus.SKIPPED,

                        sourceWeight =
                            CHALLENGE_SOURCE_WEIGHT
                    )
                }
                .toList()

        val targetedMissionEvidence =
            missionEvidence
                .asSequence()
                .filter { evidence ->
                    evidence.capabilityId ==
                            capabilityId
                }
                .map { evidence ->
                    MasteryEvidenceItem(
                        value =
                            evidence
                                .weightedEvidenceValue,

                        timestamp =
                            evidence.evaluatedAt,

                        completed =
                            evidence.demonstrated,

                        skipped =
                            false,

                        sourceWeight =
                            MISSION_SOURCE_WEIGHT
                    )
                }
                .toList()

        val combinedEvidence =
            (
                    challengeEvidence +
                            targetedMissionEvidence
                    )
                .sortedByDescending { evidence ->
                    evidence.timestamp
                }
                .take(
                    MAXIMUM_COUNTED_ATTEMPTS
                )

        if (combinedEvidence.isEmpty()) {
            return CapabilityMastery.empty(
                capabilityId =
                    capabilityId
            )
        }

        var weightedEvidence =
            0f

        var totalWeight =
            0f

        combinedEvidence.forEachIndexed { index, evidence ->
            val recencyWeight =
                RECENCY_WEIGHTS.getOrElse(
                    index =
                        index
                ) {
                    MINIMUM_RECENCY_WEIGHT
                }

            val finalWeight =
                recencyWeight *
                        evidence.sourceWeight

            weightedEvidence +=
                evidence.value *
                        finalWeight

            totalWeight +=
                finalWeight
        }

        val masteryScore =
            if (totalWeight > 0f) {
                weightedEvidence /
                        totalWeight
            } else {
                0f
            }
                .coerceIn(
                    minimumValue =
                        0f,

                    maximumValue =
                        1f
                )

        val completedCount =
            combinedEvidence.count { evidence ->
                evidence.completed
            }

        val skippedCount =
            combinedEvidence.count { evidence ->
                evidence.skipped
            }

        return CapabilityMastery(
            capabilityId =
                capabilityId,

            attemptCount =
                combinedEvidence.size,

            completedCount =
                completedCount,

            skippedCount =
                skippedCount,

            masteryScore =
                masteryScore,

            status =
                determineStatus(
                    masteryScore =
                        masteryScore,

                    completedCount =
                        completedCount
                ),

            lastChallengedAt =
                combinedEvidence
                    .maxOfOrNull { evidence ->
                        evidence.timestamp
                    }
        )
    }

    private fun BesserChallenge.evidenceValue():
            Float {

        if (
            status !=
            ChallengeStatus.COMPLETED
        ) {
            return SKIPPED_EVIDENCE
        }

        val assistanceWeight =
            actualAssistanceLevel
                ?.evidenceWeight
                ?: recommendedAssistanceLevel
                    .evidenceWeight

        val evaluationWeight =
            safeEvaluationScore
                ?.let { score ->
                    score.toFloat() /
                            100f
                }
                ?: LEGACY_EVALUATION_WEIGHT

        return (
                assistanceWeight *
                        evaluationWeight
                )
            .coerceIn(
                minimumValue =
                    0f,

                maximumValue =
                    1f
            )
    }

    private fun BesserChallenge.terminalTimestamp():
            Long {

        return completedAt
            ?: skippedAt
            ?: updatedAt
    }

    private fun determineStatus(
        masteryScore: Float,
        completedCount: Int
    ): CapabilityMasteryStatus {

        return when {
            masteryScore >=
                    MASTERED_THRESHOLD &&
                    completedCount >=
                    MINIMUM_MASTERED_COMPLETIONS -> {
                CapabilityMasteryStatus.MASTERED
            }

            masteryScore >=
                    PRACTISING_THRESHOLD -> {
                CapabilityMasteryStatus.PRACTISING
            }

            else -> {
                CapabilityMasteryStatus.DEVELOPING
            }
        }
    }

    private data class MasteryEvidenceItem(
        val value: Float,

        val timestamp: Long,

        val completed: Boolean,

        val skipped: Boolean,

        val sourceWeight: Float
    )

    private companion object {

        const val MAXIMUM_COUNTED_ATTEMPTS =
            8

        const val MASTERED_THRESHOLD =
            0.70f

        const val PRACTISING_THRESHOLD =
            0.45f

        const val MINIMUM_MASTERED_COMPLETIONS =
            3

        const val CHALLENGE_SOURCE_WEIGHT =
            1.00f

        const val MISSION_SOURCE_WEIGHT =
            0.90f

        const val SKIPPED_EVIDENCE =
            0f

        const val LEGACY_EVALUATION_WEIGHT =
            1f

        const val MINIMUM_RECENCY_WEIGHT =
            0.40f

        val RECENCY_WEIGHTS =
            listOf(
                1.00f,
                0.92f,
                0.84f,
                0.76f,
                0.68f,
                0.60f,
                0.50f,
                0.40f
            )
    }
}