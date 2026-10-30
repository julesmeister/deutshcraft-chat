// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/usecase/BuildMissionLearningContextUseCase.kt

package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.repository.AdaptiveLearningRepository
import com.julesmeister.deutschcraft.feature.besser.domain.Capability
import com.julesmeister.deutschcraft.feature.besser.domain.mastery.CapabilityMastery
import com.julesmeister.deutschcraft.feature.besser.domain.mastery.CapabilityMasteryStatus
import com.julesmeister.deutschcraft.feature.besser.domain.progression.CapabilityProgressionEvaluator
import com.julesmeister.deutschcraft.feature.besser.domain.repository.CapabilityRepository
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.GetCapabilityMasteriesUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionLearningContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class BuildMissionLearningContextUseCase
@Inject constructor(
    private val capabilityRepository:
    CapabilityRepository,

    private val getCapabilityMasteries:
    GetCapabilityMasteriesUseCase,

    private val progressionEvaluator:
    CapabilityProgressionEvaluator,

    private val adaptiveLearningRepository:
    AdaptiveLearningRepository
) {

    suspend operator fun invoke(
        cefrLevel: CefrLevel
    ): MissionLearningContext {

        val targetLevel =
            nextLevel(
                currentLevel =
                    cefrLevel
            )

        val relevantCapabilities =
            capabilityRepository
                .getActiveForShortlist(
                    cefrLevels =
                        listOf(
                            cefrLevel.name,
                            targetLevel.name
                        )
                            .distinct()
                )

        val capabilityIds =
            relevantCapabilities
                .asSequence()
                .map { capability ->
                    capability.id
                }
                .toSet()

        val prerequisiteIds =
            capabilityIds
                .asSequence()
                .flatMap { capabilityId ->
                    progressionEvaluator
                        .prerequisitesFor(
                            capabilityId =
                                capabilityId
                        )
                        .asSequence()
                }
                .toSet()

        val masteryByCapabilityId =
            getCapabilityMasteries(
                capabilityIds =
                    capabilityIds +
                            prerequisiteIds
            )

        val capabilitySignals =
            relevantCapabilities.map { capability ->
                capability.toSignal(
                    mastery =
                        masteryByCapabilityId[
                            capability.id
                        ]
                            ?: CapabilityMastery.empty(
                                capabilityId =
                                    capability.id
                            ),

                    masteryByCapabilityId =
                        masteryByCapabilityId
                )
            }

        val reinforcementFocuses =
            adaptiveLearningRepository
                .getTopReinforcementFocuses(
                    minimumConfidence =
                        MINIMUM_ADAPTIVE_CONFIDENCE,

                    limit =
                        MAXIMUM_REINFORCEMENT_FOCUS_COUNT
                )
                .asSequence()
                .filter { focus ->
                    focus.targetCefrLevel ==
                            cefrLevel ||
                            focus.targetCefrLevel ==
                            targetLevel
                }
                .map { focus ->
                    buildString {
                        append(
                            focus.focusKey
                        )

                        append(
                            " | "
                        )

                        append(
                            focus.title
                        )

                        append(
                            " | "
                        )

                        append(
                            focus.reinforcementInstruction
                        )
                    }
                }
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .distinct()
                .take(
                    MAXIMUM_REINFORCEMENT_FOCUS_COUNT
                )
                .toList()

        val progressionReadyCapabilities =
            capabilitySignals
                .asSequence()
                .filter { signal ->
                    signal.mastery.status !=
                            CapabilityMasteryStatus.MASTERED
                }
                .filter { signal ->
                    signal.progressionReady ||
                            signal.directSuccessorOfMastered
                }
                .sortedWith(
                    compareByDescending<CapabilitySignal> { signal ->
                        signal.directSuccessorOfMastered
                    }
                        .thenByDescending { signal ->
                            signal.progressionReady
                        }
                        .thenByDescending { signal ->
                            signal.prerequisiteMastery
                        }
                        .thenBy { signal ->
                            signal.mastery.safeMasteryScore
                        }
                )
                .map { signal ->
                    signal.capability.toMissionSignal(
                        prefix =
                            if (
                                signal
                                    .directSuccessorOfMastered
                            ) {
                                "next_step"
                            } else {
                                "progression_ready"
                            },

                        score =
                            signal.prerequisiteMastery
                    )
                }
                .distinct()
                .take(
                    MAXIMUM_PROGRESSION_CAPABILITY_COUNT
                )
                .toList()

        val developingCapabilities =
            capabilitySignals
                .asSequence()
                .filter { signal ->
                    signal.mastery.status ==
                            CapabilityMasteryStatus.DEVELOPING
                }
                .filter { signal ->
                    signal.mastery.hasHistory
                }
                .sortedWith(
                    compareBy<CapabilitySignal> { signal ->
                        signal.mastery.safeMasteryScore
                    }
                        .thenByDescending { signal ->
                            signal.mastery.attemptCount
                        }
                )
                .map { signal ->
                    signal.capability.toMissionSignal(
                        prefix =
                            "developing",

                        score =
                            signal.mastery.safeMasteryScore
                    )
                }
                .distinct()
                .take(
                    MAXIMUM_DEVELOPING_CAPABILITY_COUNT
                )
                .toList()

        val masteredCapabilities =
            capabilitySignals
                .asSequence()
                .filter { signal ->
                    signal.mastery.status ==
                            CapabilityMasteryStatus.MASTERED
                }
                .sortedByDescending { signal ->
                    signal.mastery.safeMasteryScore
                }
                .map { signal ->
                    signal.capability.toMissionSignal(
                        prefix =
                            "mastered",

                        score =
                            signal.mastery.safeMasteryScore
                    )
                }
                .distinct()
                .take(
                    MAXIMUM_MASTERED_CAPABILITY_COUNT
                )
                .toList()

        return MissionLearningContext(
            reinforcementFocuses =
                reinforcementFocuses,

            progressionReadyCapabilities =
                progressionReadyCapabilities,

            developingCapabilities =
                developingCapabilities,

            masteredCapabilities =
                masteredCapabilities
        )
    }

    private fun Capability.toSignal(
        mastery: CapabilityMastery,
        masteryByCapabilityId:
        Map<String, CapabilityMastery>
    ): CapabilitySignal {

        val progression =
            progressionEvaluator.evaluate(
                capabilityId =
                    id,

                masteryByCapabilityId =
                    masteryByCapabilityId
            )

        return CapabilitySignal(
            capability =
                this,

            mastery =
                mastery,

            progressionReady =
                progression.isProgressionReady,

            directSuccessorOfMastered =
                progression
                    .isDirectSuccessorOfMasteredCapability,

            prerequisiteMastery =
                progression
                    .safeAveragePrerequisiteMastery
        )
    }

    private fun Capability.toMissionSignal(
        prefix: String,
        score: Float
    ): String {

        return buildString {
            append(
                prefix
            )

            append(
                ":"
            )

            append(
                id
            )

            append(
                " | "
            )

            append(
                name
            )

            append(
                " | category="
            )

            append(
                category.name.lowercase()
            )

            append(
                " | cefr="
            )

            append(
                cefrLevel.uppercase()
            )

            append(
                " | score="
            )

            append(
                (
                        score.coerceIn(
                            minimumValue =
                                0f,

                            maximumValue =
                                1f
                        ) *
                                100f
                        )
                    .toInt()
            )

            append(
                "%"
            )
        }
    }

    private fun nextLevel(
        currentLevel: CefrLevel
    ): CefrLevel {

        return when (currentLevel) {
            CefrLevel.A1 ->
                CefrLevel.A2

            CefrLevel.A2 ->
                CefrLevel.B1

            CefrLevel.B1 ->
                CefrLevel.B2

            CefrLevel.B2 ->
                CefrLevel.C1

            CefrLevel.C1 ->
                CefrLevel.C2

            CefrLevel.C2 ->
                CefrLevel.C2
        }
    }

    private data class CapabilitySignal(
        val capability: Capability,

        val mastery: CapabilityMastery,

        val progressionReady: Boolean,

        val directSuccessorOfMastered: Boolean,

        val prerequisiteMastery: Float
    )

    private companion object {

        const val MINIMUM_ADAPTIVE_CONFIDENCE =
            0.55f

        const val MAXIMUM_REINFORCEMENT_FOCUS_COUNT =
            3

        const val MAXIMUM_PROGRESSION_CAPABILITY_COUNT =
            4

        const val MAXIMUM_DEVELOPING_CAPABILITY_COUNT =
            3

        const val MAXIMUM_MASTERED_CAPABILITY_COUNT =
            5
    }
}