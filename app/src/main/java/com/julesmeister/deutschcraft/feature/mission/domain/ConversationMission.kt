// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/ConversationMission.kt

package com.julesmeister.deutschcraft.feature.mission.domain

data class ConversationMission(
    val id: String,

    val conversationId: String,

    val template:
    MissionTemplate,

    val sourceRecommendationId: String? =
        null,

    val targetCapabilityIds:
    Set<String> =
        emptySet(),

    val adaptiveFocusKeys:
    Set<String> =
        emptySet(),

    val status:
    MissionStatus,

    val objectiveProgress:
    List<MissionObjectiveProgress>,

    val startedAt: Long?,

    val completedAt: Long?,

    val dismissedAt: Long?,

    val createdAt: Long,

    val updatedAt: Long
) {

    val hasLearningTargets: Boolean
        get() =
            targetCapabilityIds.isNotEmpty() ||
                    adaptiveFocusKeys.isNotEmpty()

    val progress: MissionProgress
        get() {
            val requiredObjectives =
                objectiveProgress.filter { progress ->
                    progress.objective.isRequired
                }

            val optionalObjectives =
                objectiveProgress.filter { progress ->
                    !progress.objective.isRequired
                }

            val completedRequired =
                requiredObjectives.count { progress ->
                    progress.isCompleted
                }

            val completedOptional =
                optionalObjectives.count { progress ->
                    progress.isCompleted
                }

            val percentage =
                if (requiredObjectives.isEmpty()) {
                    0
                } else {
                    (
                            completedRequired *
                                    100 /
                                    requiredObjectives.size
                            )
                        .coerceIn(
                            minimumValue =
                                0,

                            maximumValue =
                                100
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
                .sortedBy { progress ->
                    progress.objective.sequenceNumber
                }
                .firstOrNull { progress ->
                    progress.objective.isRequired &&
                            !progress.isCompleted
                }
}