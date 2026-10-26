package com.julesmeister.deutschcraft.feature.mission.domain

import javax.inject.Inject
import kotlin.math.roundToInt

/*
 * Converts the complete persisted mission into the smaller
 * mission context used by Emil's reply generator.
 *
 * The resulting context tells Emil:
 *
 * - Which scenario is active
 * - Which role Emil must temporarily adopt
 * - Which role the learner has
 * - Which objective is currently active
 * - Which objectives remain unfinished
 * - How far the learner has progressed
 */
class MissionConversationContextMapper
@Inject constructor() {

    fun map(
        mission: ConversationMission?
    ): ActiveMissionConversationContext? {
        if (
            mission == null ||
            mission.status != MissionStatus.ACTIVE
        ) {
            return null
        }

        val orderedProgress =
            mission.objectiveProgress

        if (orderedProgress.isEmpty()) {
            return null
        }

        val unfinishedProgress =
            orderedProgress.filter { progress ->
                !progress.isCompleted
            }

        /*
         * Prefer the mission's explicitly selected current
         * objective.
         *
         * If that objective is missing or already completed,
         * fall back to the first unfinished objective.
         */
        val currentProgress =
            mission.currentObjective
                ?.takeIf { progress ->
                    !progress.isCompleted
                }
                ?: unfinishedProgress.firstOrNull()
                ?: return null

        /*
         * Keep the current objective first, followed by the
         * other unfinished objectives in their persisted
         * order.
         *
         * This makes the prompt predictable for Emil.
         */
        val remainingObjectives =
            buildList {
                add(
                    currentProgress
                        .objective
                        .toConversationObjective()
                )

                unfinishedProgress
                    .asSequence()
                    .filter { progress ->
                        progress.objective.id !=
                                currentProgress.objective.id
                    }
                    .map { progress ->
                        progress
                            .objective
                            .toConversationObjective()
                    }
                    .forEach(
                        ::add
                    )
            }

        return ActiveMissionConversationContext(
            missionId =
                mission.id,
            title =
                mission.template.title,
            scenario =
                mission.template.scenario,
            learnerRole =
                mission.template.learnerRole,
            assistantRole =
                mission.template.assistantRole,
            progressPercentage =
                calculateProgressPercentage(
                    mission =
                        mission
                ),
            currentObjective =
                currentProgress
                    .objective
                    .toConversationObjective(),
            remainingObjectives =
                remainingObjectives
        )
    }

    /*
     * Calculates progress directly from Room-backed
     * objective-progress records because ConversationMission
     * does not expose a progressPercentage property.
     *
     * Example:
     *
     * 5 completed objectives
     * 10 total objectives
     *
     * 5 / 10 = 50%
     */
    private fun calculateProgressPercentage(
        mission: ConversationMission
    ): Int {
        val totalObjectiveCount =
            mission.objectiveProgress.size

        if (totalObjectiveCount <= 0) {
            return 0
        }

        val completedObjectiveCount =
            mission.objectiveProgress.count { progress ->
                progress.isCompleted
            }

        return (
                completedObjectiveCount.toFloat() /
                        totalObjectiveCount.toFloat() *
                        PERCENTAGE_MULTIPLIER
                )
            .roundToInt()
            .coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    100
            )
    }

    private fun MissionObjective.toConversationObjective():
            MissionConversationObjective {
        return MissionConversationObjective(
            id =
                id,
            title =
                title,
            description =
                description,
            coachingHints =
                coachingHints,
            examplePhrases =
                examplePhrases,
            targetVocabulary =
                targetVocabulary
        )
    }

    private companion object {
        const val PERCENTAGE_MULTIPLIER =
            100f
    }
}