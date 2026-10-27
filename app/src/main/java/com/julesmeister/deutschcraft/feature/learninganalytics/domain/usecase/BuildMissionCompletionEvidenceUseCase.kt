package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionCompletionEvidence
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionObjectiveEvidence
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionObjectiveHintUsageRepository
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionDifficulty
import javax.inject.Inject

/*
 * Builds the evidence consumed by
 * CalculateMissionAnalyticsUseCase.
 *
 * Real evidence sources:
 *
 * - Objective completion
 * - Objective evaluation confidence
 * - Learner-visible hints
 * - Besser corrections and resolution
 * - Target-vocabulary demonstration
 * - Interaction behavior
 * - Comprehension/relevant replies
 */
class BuildMissionCompletionEvidenceUseCase
@Inject constructor(
    private val hintUsageRepository:
    MissionObjectiveHintUsageRepository,
    private val buildMissionBesserEvidence:
    BuildMissionBesserEvidenceUseCase,
    private val buildMissionVocabularyEvidence:
    BuildMissionVocabularyEvidenceUseCase,
    private val buildMissionInteractionEvidence:
    BuildMissionInteractionEvidenceUseCase
) {

    suspend operator fun invoke(
        mission:
        ConversationMission
    ): MissionCompletionEvidence {
        validateMission(
            mission =
                mission
        )

        val hintsByObjective =
            hintUsageRepository
                .getMissionHintUsage(
                    conversationMissionId =
                        mission.id
                )
                .groupBy { usage ->
                    usage.objectiveId
                }

        val besserEvidence =
            buildMissionBesserEvidence(
                mission =
                    mission
            )

        val vocabularyEvidence =
            buildMissionVocabularyEvidence(
                mission =
                    mission
            )

        val interactionEvidence =
            buildMissionInteractionEvidence(
                mission =
                    mission
            )

        val objectiveEvidence =
            mission
                .objectiveProgress
                .sortedBy { progress ->
                    progress
                        .objective
                        .sequenceNumber
                }
                .map { progress ->
                    val objective =
                        progress.objective

                    val highestHintLevel =
                        hintsByObjective[
                            objective.id
                        ]
                            .orEmpty()
                            .maxOfOrNull { usage ->
                                usage.hintLevel
                            }
                            ?.coerceIn(
                                minimumValue =
                                    NO_HINT_LEVEL,
                                maximumValue =
                                    MAXIMUM_HINT_LEVEL
                            )
                            ?: NO_HINT_LEVEL

                    MissionObjectiveEvidence(
                        objectiveId =
                            objective.id,
                        title =
                            objective.title,
                        isRequired =
                            objective.isRequired,
                        wasCompleted =
                            progress.isCompleted,
                        hintsUsed =
                            highestHintLevel,
                        besserCorrectionCount =
                            besserEvidence
                                .correctionsByObjectiveId[
                                objective.id
                            ]
                                ?: 0,
                        evaluationConfidence =
                            progress.normalizedConfidence,
                        targetVocabulary =
                            objective
                                .targetVocabulary
                                .normalizedVocabularySet(),
                        correctlyUsedVocabulary =
                            vocabularyEvidence
                                .usedByObjectiveId[
                                objective.id
                            ]
                                .orEmpty()
                    )
                }

        return MissionCompletionEvidence(
            missionId =
                mission.id,
            conversationId =
                mission.conversationId,
            cefrLevel =
                mission
                    .template
                    .difficulty
                    .toCefrLevel(),
            objectiveEvidence =
                objectiveEvidence,
            learnerMessageCount =
                besserEvidence.learnerMessageCount,
            besserCorrectionCount =
                besserEvidence.correctedMessageCount,
            resolvedBesserCorrectionCount =
                besserEvidence.resolvedCorrectionCount,
            correctlyUsedTargetVocabulary =
                vocabularyEvidence
                    .correctlyUsedVocabulary,
            availableTargetVocabulary =
                vocabularyEvidence
                    .availableVocabulary,
            independentFollowUpQuestionCount =
                interactionEvidence
                    .independentFollowUpQuestionCount,
            successfulClarificationCount =
                interactionEvidence
                    .successfulClarificationCount,
            contextuallyRelevantReplyCount =
                interactionEvidence
                    .contextuallyRelevantReplyCount,
            expectedReplyCount =
                interactionEvidence
                    .expectedReplyCount,
            startedAt =
                mission.startedAt
                    ?: mission.createdAt,
            completedAt =
                mission.completedAt
        )
    }

    private fun validateMission(
        mission:
        ConversationMission
    ) {
        require(
            mission.id.isNotBlank()
        ) {
            "Conversation mission ID must not be blank."
        }

        require(
            mission.conversationId.isNotBlank()
        ) {
            "Conversation ID must not be blank."
        }

        require(
            mission.objectiveProgress.isNotEmpty()
        ) {
            "Mission must contain objective progress."
        }
    }

    private fun List<String>.normalizedVocabularySet():
            Set<String> {
        return asSequence()
            .map(
                String::trim
            )
            .filter(
                String::isNotBlank
            )
            .distinctBy { value ->
                value.lowercase()
            }
            .toSet()
    }

    private fun MissionDifficulty.toCefrLevel():
            CefrLevel {
        return CefrLevel.valueOf(
            name
        )
    }

    private companion object {
        const val NO_HINT_LEVEL =
            0

        const val MAXIMUM_HINT_LEVEL =
            3
    }
}