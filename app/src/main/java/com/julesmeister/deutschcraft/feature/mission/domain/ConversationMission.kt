package com.julesmeister.deutschcraft.feature.mission.domain

data class ConversationMission(
    val id: String,
    val conversationId: String,
    val template: MissionTemplate,
    val status: MissionStatus,
    val objectiveProgress:
    List<MissionObjectiveProgress>,
    val startedAt: Long?,
    val completedAt: Long?,
    val dismissedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
) {

    val progress: MissionProgress
        get() {
            val requiredObjectives =
                objectiveProgress.filter {
                    it.objective.isRequired
                }

            val optionalObjectives =
                objectiveProgress.filter {
                    !it.objective.isRequired
                }

            val completedRequired =
                requiredObjectives.count {
                    it.isCompleted
                }

            val completedOptional =
                optionalObjectives.count {
                    it.isCompleted
                }

            val percentage =
                if (requiredObjectives.isEmpty()) {
                    0
                } else {
                    (
                            completedRequired * 100 /
                                    requiredObjectives.size
                            ).coerceIn(
                            minimumValue = 0,
                            maximumValue = 100
                        )
                }

            return MissionProgress(
                completedRequiredObjectives =
                    completedRequired,
                totalRequiredObjectives =
                    requiredObjectives.size,
                completedOptionalObjectives =
                    completedOptional,
                totalOptionalObjectives =
                    optionalObjectives.size,
                percentage =
                    percentage
            )
        }

    val currentObjective:
            MissionObjectiveProgress?
        get() =
            objectiveProgress
                .sortedBy {
                    it.objective.sequenceNumber
                }
                .firstOrNull {
                    it.objective.isRequired &&
                            !it.isCompleted
                }
}