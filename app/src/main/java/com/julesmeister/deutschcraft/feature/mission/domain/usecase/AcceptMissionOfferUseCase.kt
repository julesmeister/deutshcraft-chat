// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/usecase/AcceptMissionOfferUseCase.kt

package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionOfferCache
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjective
import com.julesmeister.deutschcraft.feature.mission.domain.MissionTemplate
import com.julesmeister.deutschcraft.feature.mission.domain.repository.MissionRepository
import java.util.UUID
import javax.inject.Inject

class AcceptMissionOfferUseCase
@Inject constructor(
    private val missionRepository:
    MissionRepository,

    private val missionOfferCache:
    MissionOfferCache
) {

    suspend operator fun invoke(
        conversationId: String,

        sourceRecommendationId: String? =
            null,

        targetCapabilityIds:
        Set<String> =
            emptySet(),

        adaptiveFocusKeys:
        Set<String> =
            emptySet()
    ): String {

        val opportunity =
            missionOfferCache.getOffer(
                conversationId =
                    conversationId
            )
                ?: throw IllegalStateException(
                    "No mission offer is available."
                )

        val templateId =
            UUID.randomUUID().toString()

        val template =
            MissionTemplate(
                id =
                    templateId,

                title =
                    opportunity.mission.title,

                description =
                    opportunity.mission.description,

                scenario =
                    opportunity.mission.scenario,

                learnerRole =
                    opportunity.mission.learnerRole,

                assistantRole =
                    opportunity.mission.assistantRole,

                difficulty =
                    opportunity.mission.difficulty,

                icon =
                    opportunity.mission.icon,

                objectives =
                    opportunity
                        .mission
                        .objectives
                        .mapIndexed { index, objective ->
                            MissionObjective(
                                id =
                                    UUID
                                        .randomUUID()
                                        .toString(),

                                missionTemplateId =
                                    templateId,

                                title =
                                    objective.title,

                                description =
                                    objective.description,

                                sequenceNumber =
                                    index +
                                            1,

                                isRequired =
                                    objective.isRequired,

                                completionHint =
                                    objective.completionHint,

                                coachingHints =
                                    objective
                                        .sanitizedCoachingHints,

                                targetVocabulary =
                                    objective
                                        .sanitizedTargetVocabulary,

                                examplePhrases =
                                    objective
                                        .sanitizedExamplePhrases
                            )
                        },

                isEnabled =
                    true
            )

        missionRepository.saveMissionTemplate(
            template =
                template
        )

        val conversationMissionId =
            missionRepository.offerMission(
                conversationId =
                    conversationId,

                missionTemplateId =
                    template.id,

                sourceRecommendationId =
                    sourceRecommendationId,

                targetCapabilityIds =
                    targetCapabilityIds,

                adaptiveFocusKeys =
                    adaptiveFocusKeys
            )

        missionRepository.startMission(
            conversationMissionId =
                conversationMissionId
        )

        missionOfferCache.clearOffer(
            conversationId =
                conversationId
        )

        return conversationMissionId
    }
}