package com.julesmeister.deutschcraft.feature.mission.data.mapper

import com.julesmeister.deutschcraft.core.database.entity.mission.ConversationMissionEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveProgressEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionTemplateEntity
import com.julesmeister.deutschcraft.core.database.relation.ConversationMissionWithDetails
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionDifficulty
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjective
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjectiveProgress
import com.julesmeister.deutschcraft.feature.mission.domain.MissionStatus
import com.julesmeister.deutschcraft.feature.mission.domain.MissionTemplate

/*
 * Mission template mappings
 */

fun MissionTemplateEntity.toDomain(
    objectives: List<MissionObjectiveEntity>
): MissionTemplate {
    return MissionTemplate(
        id = id,
        title = title,
        description = description,
        scenario = scenario,
        learnerRole = learnerRole,
        assistantRole = assistantRole,
        difficulty =
            difficulty.toMissionDifficulty(),
        icon = icon,
        objectives =
            objectives
                .filter { objective ->
                    objective.missionTemplateId == id
                }
                .sortedBy { objective ->
                    objective.sequenceNumber
                }
                .map { objective ->
                    objective.toDomain()
                },
        isEnabled = isEnabled
    )
}

fun MissionTemplate.toEntity(
    createdAt: Long,
    updatedAt: Long
): MissionTemplateEntity {
    return MissionTemplateEntity(
        id = id,
        title = title,
        description = description,
        scenario = scenario,
        learnerRole = learnerRole,
        assistantRole = assistantRole,
        difficulty = difficulty.name,
        icon = icon,
        isEnabled = isEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/*
 * Mission objective mappings
 */

fun MissionObjectiveEntity.toDomain():
        MissionObjective {
    return MissionObjective(
        id = id,
        missionTemplateId =
            missionTemplateId,
        title = title,
        description = description,
        sequenceNumber =
            sequenceNumber,
        isRequired = isRequired,
        completionHint =
            completionHint
    )
}

fun MissionObjective.toEntity():
        MissionObjectiveEntity {
    return MissionObjectiveEntity(
        id = id,
        missionTemplateId =
            missionTemplateId,
        title = title,
        description = description,
        sequenceNumber =
            sequenceNumber,
        isRequired = isRequired,
        completionHint =
            completionHint
    )
}

/*
 * Conversation mission mappings
 */

fun ConversationMissionEntity.toDomain(
    template: MissionTemplate,
    objectiveProgress:
    List<MissionObjectiveProgress>
): ConversationMission {
    return ConversationMission(
        id = id,
        conversationId =
            conversationId,
        template = template,
        status =
            status.toMissionStatus(),
        objectiveProgress =
            objectiveProgress.sortedBy {
                    progress ->
                progress.objective.sequenceNumber
            },
        startedAt = startedAt,
        completedAt = completedAt,
        dismissedAt = dismissedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ConversationMission.toEntity():
        ConversationMissionEntity {
    return ConversationMissionEntity(
        id = id,
        conversationId =
            conversationId,
        missionTemplateId =
            template.id,
        status = status.name,
        startedAt = startedAt,
        completedAt = completedAt,
        dismissedAt = dismissedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/*
 * Objective-progress mappings
 */

fun MissionObjectiveProgressEntity.toDomain(
    objective: MissionObjective
): MissionObjectiveProgress {
    return MissionObjectiveProgress(
        objective = objective,
        isCompleted = isCompleted,
        completedAt = completedAt,
        completedByMessageId =
            completedByMessageId,
        confidence =
            confidence.coerceIn(
                minimumValue = 0f,
                maximumValue = 1f
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

/*
 * Complete Room-relation mapping
 */

fun ConversationMissionWithDetails.toDomain():
        ConversationMission {
    val templateId =
        conversationMission.missionTemplateId

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

/*
 * Safe enum conversions
 */

private fun String.toMissionDifficulty():
        MissionDifficulty {
    return enumValues<MissionDifficulty>()
        .firstOrNull { difficulty ->
            difficulty.name.equals(
                other = trim(),
                ignoreCase = true
            )
        }
        ?: MissionDifficulty.A1
}

private fun String.toMissionStatus():
        MissionStatus {
    return enumValues<MissionStatus>()
        .firstOrNull { missionStatus ->
            missionStatus.name.equals(
                other = trim(),
                ignoreCase = true
            )
        }
        ?: MissionStatus.OFFERED
}