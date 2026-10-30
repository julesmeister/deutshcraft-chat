// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/data/mapper/MissionMappers.kt

package com.julesmeister.deutschcraft.feature.mission.data.mapper

import com.julesmeister.deutschcraft.core.database.entity.mission.ConversationMissionEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveProgressEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionTemplateEntity
import com.julesmeister.deutschcraft.core.database.relation.ConversationMissionWithDetails
import com.julesmeister.deutschcraft.feature.mission.data.local.toMissionObjectiveJson
import com.julesmeister.deutschcraft.feature.mission.data.local.toMissionObjectiveStringList
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionDifficulty
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjective
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjectiveProgress
import com.julesmeister.deutschcraft.feature.mission.domain.MissionStatus
import com.julesmeister.deutschcraft.feature.mission.domain.MissionTemplate
import kotlinx.serialization.json.Json

private val mapperJson =
    Json {
        ignoreUnknownKeys =
            true
    }

fun MissionTemplateEntity.toDomain(
    objectives:
    List<MissionObjectiveEntity>
): MissionTemplate {

    return MissionTemplate(
        id =
            id,

        title =
            title,

        description =
            description,

        scenario =
            scenario,

        learnerRole =
            learnerRole,

        assistantRole =
            assistantRole,

        difficulty =
            difficulty.toMissionDifficulty(),

        icon =
            icon,

        objectives =
            objectives
                .filter { objective ->
                    objective.missionTemplateId ==
                            id
                }
                .sortedBy { objective ->
                    objective.sequenceNumber
                }
                .map { objective ->
                    objective.toDomain()
                },

        isEnabled =
            isEnabled
    )
}

fun MissionTemplate.toEntity(
    createdAt: Long,
    updatedAt: Long
): MissionTemplateEntity {

    return MissionTemplateEntity(
        id =
            id,

        title =
            title,

        description =
            description,

        scenario =
            scenario,

        learnerRole =
            learnerRole,

        assistantRole =
            assistantRole,

        difficulty =
            difficulty.name,

        icon =
            icon,

        isEnabled =
            isEnabled,

        createdAt =
            createdAt,

        updatedAt =
            updatedAt
    )
}

fun MissionObjectiveEntity.toDomain():
        MissionObjective {

    return MissionObjective(
        id =
            id,

        missionTemplateId =
            missionTemplateId,

        title =
            title,

        description =
            description,

        sequenceNumber =
            sequenceNumber,

        isRequired =
            isRequired,

        completionHint =
            completionHint,

        coachingHints =
            coachingHintsJson
                .toMissionObjectiveStringList(
                    json =
                        mapperJson
                ),

        targetVocabulary =
            targetVocabularyJson
                .toMissionObjectiveStringList(
                    json =
                        mapperJson
                ),

        examplePhrases =
            examplePhrasesJson
                .toMissionObjectiveStringList(
                    json =
                        mapperJson
                )
    )
}

fun MissionObjective.toEntity():
        MissionObjectiveEntity {

    return MissionObjectiveEntity(
        id =
            id,

        missionTemplateId =
            missionTemplateId,

        title =
            title,

        description =
            description,

        sequenceNumber =
            sequenceNumber,

        isRequired =
            isRequired,

        completionHint =
            completionHint,

        coachingHintsJson =
            coachingHints
                .toMissionObjectiveJson(
                    json =
                        mapperJson
                ),

        targetVocabularyJson =
            targetVocabulary
                .toMissionObjectiveJson(
                    json =
                        mapperJson
                ),

        examplePhrasesJson =
            examplePhrases
                .toMissionObjectiveJson(
                    json =
                        mapperJson
                )
    )
}

fun ConversationMissionEntity.toDomain(
    template: MissionTemplate,

    objectiveProgress:
    List<MissionObjectiveProgress>
): ConversationMission {

    return ConversationMission(
        id =
            id,

        conversationId =
            conversationId,

        template =
            template,

        sourceRecommendationId =
            sourceRecommendationId,

        targetCapabilityIds =
            targetCapabilityIdsJson
                .toMissionObjectiveStringList(
                    json =
                        mapperJson
                )
                .toSet(),

        adaptiveFocusKeys =
            adaptiveFocusKeysJson
                .toMissionObjectiveStringList(
                    json =
                        mapperJson
                )
                .toSet(),

        status =
            status.toMissionStatus(),

        objectiveProgress =
            objectiveProgress
                .sortedBy { progress ->
                    progress
                        .objective
                        .sequenceNumber
                },

        startedAt =
            startedAt,

        completedAt =
            completedAt,

        dismissedAt =
            dismissedAt,

        createdAt =
            createdAt,

        updatedAt =
            updatedAt
    )
}

fun ConversationMission.toEntity():
        ConversationMissionEntity {

    return ConversationMissionEntity(
        id =
            id,

        conversationId =
            conversationId,

        missionTemplateId =
            template.id,

        sourceRecommendationId =
            sourceRecommendationId,

        targetCapabilityIdsJson =
            targetCapabilityIds
                .toList()
                .sorted()
                .toMissionObjectiveJson(
                    json =
                        mapperJson
                ),

        adaptiveFocusKeysJson =
            adaptiveFocusKeys
                .toList()
                .sorted()
                .toMissionObjectiveJson(
                    json =
                        mapperJson
                ),

        status =
            status.name,

        startedAt =
            startedAt,

        completedAt =
            completedAt,

        dismissedAt =
            dismissedAt,

        createdAt =
            createdAt,

        updatedAt =
            updatedAt
    )
}

fun MissionObjectiveProgressEntity.toDomain(
    objective: MissionObjective
): MissionObjectiveProgress {

    return MissionObjectiveProgress(
        objective =
            objective,

        isCompleted =
            isCompleted,

        completedAt =
            completedAt,

        completedByMessageId =
            completedByMessageId,

        confidence =
            confidence.coerceIn(
                minimumValue =
                    0f,

                maximumValue =
                    1f
            )
    )
}

fun MissionObjectiveProgress.toEntity(
    conversationMissionId: String,
    updatedAt: Long
): MissionObjectiveProgressEntity {

    return MissionObjectiveProgressEntity(
        conversationMissionId =
            conversationMissionId,

        objectiveId =
            objective.id,

        isCompleted =
            isCompleted,

        completedAt =
            completedAt,

        completedByMessageId =
            completedByMessageId,

        confidence =
            normalizedConfidence,

        updatedAt =
            updatedAt
    )
}

fun ConversationMissionWithDetails.toDomain():
        ConversationMission {

    val templateId =
        conversationMission
            .missionTemplateId

    val matchingObjectives =
        objectives
            .filter { objective ->
                objective.missionTemplateId ==
                        templateId
            }
            .sortedBy { objective ->
                objective.sequenceNumber
            }

    val progressByObjectiveId =
        objectiveProgress
            .filter { progress ->
                progress.conversationMissionId ==
                        conversationMission.id
            }
            .associateBy { progress ->
                progress.objectiveId
            }

    val domainObjectiveProgress =
        matchingObjectives.map { objectiveEntity ->
            val domainObjective =
                objectiveEntity.toDomain()

            val progressEntity =
                progressByObjectiveId[
                    objectiveEntity.id
                ]

            if (progressEntity == null) {
                MissionObjectiveProgress(
                    objective =
                        domainObjective,

                    isCompleted =
                        false,

                    completedAt =
                        null,

                    completedByMessageId =
                        null,

                    confidence =
                        0f
                )
            } else {
                progressEntity.toDomain(
                    objective =
                        domainObjective
                )
            }
        }

    val domainTemplate =
        template.toDomain(
            objectives =
                matchingObjectives
        )

    return conversationMission.toDomain(
        template =
            domainTemplate,

        objectiveProgress =
            domainObjectiveProgress
    )
}

private fun String.toMissionDifficulty():
        MissionDifficulty {

    return enumValues<MissionDifficulty>()
        .firstOrNull { difficulty ->
            difficulty.name.equals(
                other =
                    trim(),

                ignoreCase =
                    true
            )
        }
        ?: MissionDifficulty.A1
}

private fun String.toMissionStatus():
        MissionStatus {

    return enumValues<MissionStatus>()
        .firstOrNull { missionStatus ->
            missionStatus.name.equals(
                other =
                    trim(),

                ignoreCase =
                    true
            )
        }
        ?: MissionStatus.OFFERED
}