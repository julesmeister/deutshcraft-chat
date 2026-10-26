package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionOpportunityAiGateway
import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionOfferCache
import com.julesmeister.deutschcraft.feature.mission.domain.MissionDifficulty
import com.julesmeister.deutschcraft.feature.mission.domain.generated.GeneratedMissionDraft
import com.julesmeister.deutschcraft.feature.mission.domain.generated.GeneratedMissionObjectiveDraft
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity
import javax.inject.Inject

class DetectMissionOpportunityUseCase @Inject constructor(
    private val aiGateway:
    MissionOpportunityAiGateway,
    private val missionOfferCache:
    MissionOfferCache
) {

    suspend operator fun invoke(
        conversationId: String,
        conversationHistory: List<ChatMessage>,
        cefrLevel: CefrLevel,
        hasActiveMission: Boolean
    ): MissionOpportunity? {
        if (hasActiveMission) {
            return null
        }

        missionOfferCache
            .getOffer(
                conversationId =
                    conversationId
            )
            ?.let { existingOffer ->
                return existingOffer
            }

        val learnerMessageCount =
            conversationHistory.count { message ->
                message.sender ==
                        MessageSender.USER
            }

        if (
            learnerMessageCount <
            MINIMUM_LEARNER_MESSAGE_COUNT
        ) {
            return null
        }

        val lastCheckCount =
            missionOfferCache
                .getLastCheckMessageCount(
                    conversationId =
                        conversationId
                )

        if (
            learnerMessageCount -
            lastCheckCount <
            MESSAGES_BETWEEN_CHECKS
        ) {
            return null
        }

        missionOfferCache.recordCheck(
            conversationId =
                conversationId,
            learnerMessageCount =
                learnerMessageCount
        )

        val aiOpportunity =
            aiGateway.detectMissionOpportunity(
                conversationHistory =
                    conversationHistory,
                cefrLevel =
                    cefrLevel
            )

        if (
            !aiOpportunity.shouldOfferMission ||
            aiOpportunity.confidence <
            MINIMUM_CONFIDENCE
        ) {
            return null
        }

        val aiMission =
            aiOpportunity.mission
                ?: return null

        val difficulty =
            runCatching {
                MissionDifficulty.valueOf(
                    aiMission
                        .difficulty
                        .trim()
                        .uppercase()
                )
            }.getOrNull()
                ?: return null

        if (
            difficulty.name !=
            cefrLevel.name
        ) {
            return null
        }

        val objectives =
            aiMission.objectives
                .mapNotNull { objective ->
                    val title =
                        objective.title.trim()

                    val description =
                        objective.description.trim()

                    val completionHint =
                        objective
                            .completionHint
                            .trim()

                    if (
                        title.isBlank() ||
                        description.isBlank() ||
                        completionHint.isBlank()
                    ) {
                        return@mapNotNull null
                    }

                    GeneratedMissionObjectiveDraft(
                        title =
                            title,
                        description =
                            description,
                        completionHint =
                            completionHint,
                        isRequired =
                            objective.required
                    )
                }
                .distinctBy { objective ->
                    objective.title.lowercase()
                }

        if (
            objectives.size !in
            MINIMUM_OBJECTIVE_COUNT..
            MAXIMUM_OBJECTIVE_COUNT
        ) {
            return null
        }

        if (
            objectives.none { objective ->
                objective.isRequired
            }
        ) {
            return null
        }

        val title =
            aiMission.title.trim()

        if (
            title.isBlank() ||
            missionOfferCache.wasDismissed(
                conversationId =
                    conversationId,
                missionTitle =
                    title
            )
        ) {
            return null
        }

        val opportunity =
            MissionOpportunity(
                confidence =
                    aiOpportunity.confidence
                        .coerceIn(0f, 1f),
                reason =
                    aiOpportunity.reason.trim(),
                mission =
                    GeneratedMissionDraft(
                        title =
                            title,
                        description =
                            aiMission.description.trim(),
                        scenario =
                            aiMission.scenario.trim(),
                        learnerRole =
                            aiMission.learnerRole.trim(),
                        assistantRole =
                            aiMission.assistantRole.trim(),
                        difficulty =
                            difficulty,
                        icon =
                            aiMission.icon
                                .trim()
                                .ifBlank {
                                    DEFAULT_MISSION_ICON
                                },
                        objectives =
                            objectives
                    )
            )

        if (!opportunity.hasCompleteMissionData()) {
            return null
        }

        missionOfferCache.replaceOffer(
            conversationId =
                conversationId,
            opportunity =
                opportunity
        )

        return opportunity
    }

    private fun MissionOpportunity
            .hasCompleteMissionData(): Boolean {
        return mission.title.isNotBlank() &&
                mission.description.isNotBlank() &&
                mission.scenario.isNotBlank() &&
                mission.learnerRole.isNotBlank() &&
                mission.assistantRole.isNotBlank()
    }

    private companion object {
        const val MINIMUM_LEARNER_MESSAGE_COUNT =
            3

        const val MESSAGES_BETWEEN_CHECKS =
            3

        const val MINIMUM_OBJECTIVE_COUNT =
            3

        const val MAXIMUM_OBJECTIVE_COUNT =
            5

        const val MINIMUM_CONFIDENCE =
            0.80f

        const val DEFAULT_MISSION_ICON =
            "🎯"
    }
}