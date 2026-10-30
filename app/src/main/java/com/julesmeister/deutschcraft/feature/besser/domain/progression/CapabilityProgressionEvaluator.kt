// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/domain/progression/CapabilityProgressionEvaluator.kt

package com.julesmeister.deutschcraft.feature.besser.domain.progression

import com.julesmeister.deutschcraft.feature.besser.domain.mastery.CapabilityMastery
import com.julesmeister.deutschcraft.feature.besser.domain.mastery.CapabilityMasteryStatus
import javax.inject.Inject

class CapabilityProgressionEvaluator
@Inject constructor(
    private val progressionGraph:
    CapabilityProgressionGraph
) {

    fun prerequisitesFor(
        capabilityId: String
    ): Set<String> {

        return progressionGraph.prerequisitesFor(
            capabilityId =
                capabilityId
        )
    }

    fun successorsFor(
        capabilityId: String
    ): Set<String> {

        return progressionGraph.successorsFor(
            capabilityId =
                capabilityId
        )
    }

    fun evaluate(
        capabilityId: String,
        masteryByCapabilityId:
        Map<String, CapabilityMastery>
    ): CapabilityProgressionState {

        val rule =
            progressionGraph.ruleFor(
                capabilityId =
                    capabilityId
            )
                ?: return CapabilityProgressionState.open(
                    capabilityId =
                        capabilityId
                )

        val prerequisiteIds =
            rule.prerequisiteCapabilityIds

        val isDirectSuccessorOfMasteredCapability =
            isSuccessorOfMasteredCapability(
                capabilityId =
                    capabilityId,

                masteryByCapabilityId =
                    masteryByCapabilityId
            )

        if (prerequisiteIds.isEmpty()) {
            return CapabilityProgressionState(
                capabilityId =
                    capabilityId,

                prerequisiteCapabilityIds =
                    emptySet(),

                satisfiedPrerequisiteIds =
                    emptySet(),

                unsatisfiedPrerequisiteIds =
                    emptySet(),

                averagePrerequisiteMastery =
                    1f,

                isProgressionReady =
                    true,

                isDirectSuccessorOfMasteredCapability =
                    isDirectSuccessorOfMasteredCapability
            )
        }

        val satisfiedPrerequisites =
            prerequisiteIds
                .filterTo(
                    linkedSetOf()
                ) { prerequisiteId ->
                    val mastery =
                        masteryByCapabilityId[
                            prerequisiteId
                        ]

                    mastery !=
                            null &&
                            mastery.safeMasteryScore >=
                            rule.safePrerequisiteMasteryThreshold
                }

        val unsatisfiedPrerequisites =
            prerequisiteIds -
                    satisfiedPrerequisites

        val prerequisiteMasteryScores =
            prerequisiteIds
                .map { prerequisiteId ->
                    masteryByCapabilityId[
                        prerequisiteId
                    ]
                        ?.safeMasteryScore
                        ?: 0f
                }

        val averagePrerequisiteMastery =
            if (
                prerequisiteMasteryScores.isEmpty()
            ) {
                1f
            } else {
                prerequisiteMasteryScores
                    .average()
                    .toFloat()
                    .coerceIn(
                        minimumValue =
                            0f,

                        maximumValue =
                            1f
                    )
            }

        val requiredSatisfiedCount =
            requiredSatisfiedPrerequisiteCount(
                prerequisiteCount =
                    prerequisiteIds.size
            )

        val progressionReady =
            satisfiedPrerequisites.size >=
                    requiredSatisfiedCount &&
                    averagePrerequisiteMastery >=
                    rule.safePrerequisiteMasteryThreshold

        return CapabilityProgressionState(
            capabilityId =
                capabilityId,

            prerequisiteCapabilityIds =
                prerequisiteIds,

            satisfiedPrerequisiteIds =
                satisfiedPrerequisites,

            unsatisfiedPrerequisiteIds =
                unsatisfiedPrerequisites,

            averagePrerequisiteMastery =
                averagePrerequisiteMastery,

            isProgressionReady =
                progressionReady,

            isDirectSuccessorOfMasteredCapability =
                isDirectSuccessorOfMasteredCapability
        )
    }

    private fun requiredSatisfiedPrerequisiteCount(
        prerequisiteCount: Int
    ): Int {

        if (prerequisiteCount <= 0) {
            return 0
        }

        /*
         * A single prerequisite must be satisfied.
         *
         * For multiple prerequisites, require the majority
         * rather than every branch. This keeps the progression
         * graph adaptive instead of turning it into a rigid
         * lesson tree.
         */
        return (
                prerequisiteCount /
                        2
                ) +
                1
    }

    private fun isSuccessorOfMasteredCapability(
        capabilityId: String,
        masteryByCapabilityId:
        Map<String, CapabilityMastery>
    ): Boolean {

        return masteryByCapabilityId
            .values
            .asSequence()
            .filter { mastery ->
                mastery.status ==
                        CapabilityMasteryStatus.MASTERED
            }
            .any { mastery ->
                capabilityId in
                        progressionGraph.successorsFor(
                            capabilityId =
                                mastery.capabilityId
                        )
            }
    }
}