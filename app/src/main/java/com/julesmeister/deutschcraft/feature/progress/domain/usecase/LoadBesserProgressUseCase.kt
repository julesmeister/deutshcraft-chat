// app/src/main/java/com/julesmeister/deutschcraft/feature/progress/domain/usecase/LoadBesserProgressUseCase.kt

package com.julesmeister.deutschcraft.feature.progress.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.Capability
import com.julesmeister.deutschcraft.feature.besser.domain.mastery.CapabilityMastery
import com.julesmeister.deutschcraft.feature.besser.domain.mastery.CapabilityMasteryStatus
import com.julesmeister.deutschcraft.feature.besser.domain.progression.CapabilityProgressionEvaluator
import com.julesmeister.deutschcraft.feature.besser.domain.repository.CapabilityRepository
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.GetCapabilityMasteriesUseCase
import com.julesmeister.deutschcraft.feature.progress.presentation.BesserCapabilityProgressUiModel
import com.julesmeister.deutschcraft.feature.progress.presentation.BesserCategoryProgressUiModel
import com.julesmeister.deutschcraft.feature.progress.presentation.BesserProgressUiModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class LoadBesserProgressUseCase
@Inject constructor(
    private val capabilityRepository:
    CapabilityRepository,

    private val getCapabilityMasteries:
    GetCapabilityMasteriesUseCase,

    private val progressionEvaluator:
    CapabilityProgressionEvaluator
) {

    suspend operator fun invoke():
            BesserProgressUiModel {

        val capabilities =
            capabilityRepository
                .observeActive()
                .first()
                .sortedWith(
                    compareBy<Capability> { capability ->
                        cefrRank(
                            cefrLevel =
                                capability.cefrLevel
                        )
                    }
                        .thenBy { capability ->
                            capability.sortOrder
                        }
                        .thenBy { capability ->
                            capability.name.lowercase()
                        }
                )

        if (capabilities.isEmpty()) {
            return BesserProgressUiModel()
        }

        val capabilityIds =
            capabilities
                .asSequence()
                .map { capability ->
                    capability.id
                }
                .toSet()

        val prerequisiteCapabilityIds =
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
                            prerequisiteCapabilityIds
            )

        val capabilityModels =
            capabilities.map { capability ->
                val mastery =
                    masteryByCapabilityId[
                        capability.id
                    ]
                        ?: CapabilityMastery.empty(
                            capabilityId =
                                capability.id
                        )

                val progression =
                    progressionEvaluator.evaluate(
                        capabilityId =
                            capability.id,

                        masteryByCapabilityId =
                            masteryByCapabilityId
                    )

                BesserCapabilityProgressUiModel(
                    capabilityId =
                        capability.id,

                    name =
                        capability.name,

                    description =
                        capability.description,

                    category =
                        capability.category.name,

                    cefrLevel =
                        capability.cefrLevel,

                    masteryPercentage =
                        mastery.masteryPercentage,

                    masteryStatus =
                        mastery.status,

                    attemptCount =
                        mastery.attemptCount,

                    completedCount =
                        mastery.completedCount,

                    skippedCount =
                        mastery.skippedCount,

                    progressionReady =
                        progression.isProgressionReady,

                    isDirectSuccessorOfMasteredCapability =
                        progression
                            .isDirectSuccessorOfMasteredCapability,

                    prerequisiteMasteryPercentage =
                        (
                                progression
                                    .safeAveragePrerequisiteMastery *
                                        100f
                                )
                            .toInt()
                            .coerceIn(
                                minimumValue =
                                    0,

                                maximumValue =
                                    100
                            ),

                    satisfiedPrerequisiteCount =
                        progression
                            .satisfiedPrerequisiteCount,

                    prerequisiteCount =
                        progression
                            .totalPrerequisiteCount,

                    lastChallengedAt =
                        mastery.lastChallengedAt
                )
            }

        val attemptedCapabilities =
            capabilityModels.filter { capability ->
                capability.hasHistory
            }

        val overallMasteryPercentage =
            calculateAverageMastery(
                capabilities =
                    attemptedCapabilities
            )

        val categories =
            capabilityModels
                .groupBy { capability ->
                    capability.category
                }
                .map { (category, categoryCapabilities) ->
                    createCategoryProgress(
                        category =
                            category,

                        capabilities =
                            categoryCapabilities
                    )
                }
                .sortedWith(
                    compareByDescending<BesserCategoryProgressUiModel> { category ->
                        category.attemptedCapabilityCount
                    }
                        .thenByDescending { category ->
                            category.masteryPercentage
                        }
                        .thenBy { category ->
                            category.displayNameEnglish
                        }
                )

        val recommendedNextCapabilities =
            capabilityModels
                .asSequence()
                .filter { capability ->
                    capability.masteryStatus !=
                            CapabilityMasteryStatus.MASTERED
                }
                .filter { capability ->
                    capability.progressionReady ||
                            capability
                                .isDirectSuccessorOfMasteredCapability
                }
                .sortedWith(
                    compareByDescending<BesserCapabilityProgressUiModel> { capability ->
                        capability
                            .isDirectSuccessorOfMasteredCapability
                    }
                        .thenByDescending { capability ->
                            capability.progressionReady
                        }
                        .thenByDescending { capability ->
                            capability
                                .safePrerequisiteMasteryPercentage
                        }
                        .thenBy { capability ->
                            capability.safeMasteryPercentage
                        }
                        .thenBy { capability ->
                            cefrRank(
                                cefrLevel =
                                    capability.cefrLevel
                            )
                        }
                        .thenBy { capability ->
                            capability.name.lowercase()
                        }
                )
                .take(
                    RECOMMENDED_CAPABILITY_LIMIT
                )
                .toList()

        val recentCapabilities =
            capabilityModels
                .asSequence()
                .filter { capability ->
                    capability.lastChallengedAt !=
                            null
                }
                .sortedByDescending { capability ->
                    capability.lastChallengedAt
                        ?: 0L
                }
                .take(
                    RECENT_CAPABILITY_LIMIT
                )
                .toList()

        return BesserProgressUiModel(
            capabilityCount =
                capabilityModels.size,

            attemptedCapabilityCount =
                attemptedCapabilities.size,

            completedAttemptCount =
                attemptedCapabilities.sumOf { capability ->
                    capability.completedCount
                },

            masteredCapabilityCount =
                attemptedCapabilities.count { capability ->
                    capability.masteryStatus ==
                            CapabilityMasteryStatus.MASTERED
                },

            practisingCapabilityCount =
                attemptedCapabilities.count { capability ->
                    capability.masteryStatus ==
                            CapabilityMasteryStatus.PRACTISING
                },

            developingCapabilityCount =
                attemptedCapabilities.count { capability ->
                    capability.masteryStatus ==
                            CapabilityMasteryStatus.DEVELOPING
                },

            overallMasteryPercentage =
                overallMasteryPercentage,

            categories =
                categories,

            recommendedNextCapabilities =
                recommendedNextCapabilities,

            recentCapabilities =
                recentCapabilities
        )
    }

    private fun calculateAverageMastery(
        capabilities:
        List<BesserCapabilityProgressUiModel>
    ): Int {

        if (capabilities.isEmpty()) {
            return 0
        }

        return capabilities
            .map { capability ->
                capability.safeMasteryPercentage
            }
            .average()
            .toInt()
            .coerceIn(
                minimumValue =
                    0,

                maximumValue =
                    100
            )
    }

    private fun createCategoryProgress(
        category: String,
        capabilities:
        List<BesserCapabilityProgressUiModel>
    ): BesserCategoryProgressUiModel {

        val attemptedCapabilities =
            capabilities.filter { capability ->
                capability.hasHistory
            }

        return BesserCategoryProgressUiModel(
            category =
                category,

            displayNameGerman =
                categoryDisplayNameGerman(
                    category =
                        category
                ),

            displayNameEnglish =
                categoryDisplayNameEnglish(
                    category =
                        category
                ),

            masteryPercentage =
                calculateAverageMastery(
                    capabilities =
                        attemptedCapabilities
                ),

            attemptedCapabilityCount =
                attemptedCapabilities.size,

            capabilityCount =
                capabilities.size,

            masteredCapabilityCount =
                attemptedCapabilities.count { capability ->
                    capability.masteryStatus ==
                            CapabilityMasteryStatus.MASTERED
                }
        )
    }

    private fun categoryDisplayNameGerman(
        category: String
    ): String {

        return when (
            category
                .trim()
                .uppercase()
        ) {
            "COMMUNICATIVE_FUNCTION" ->
                "Kommunikation"

            "DESCRIPTION" ->
                "Beschreibung"

            "INTERACTION" ->
                "Interaktion"

            "GRAMMAR" ->
                "Grammatik"

            "DISCOURSE" ->
                "Diskurs"

            "NARRATION" ->
                "Erzählen"

            "PRAGMATICS" ->
                "Pragmatik"

            "VOCABULARY" ->
                "Wortschatz"

            else ->
                category.toDisplayName()
        }
    }

    private fun categoryDisplayNameEnglish(
        category: String
    ): String {

        return when (
            category
                .trim()
                .uppercase()
        ) {
            "COMMUNICATIVE_FUNCTION" ->
                "Communication"

            "DESCRIPTION" ->
                "Description"

            "INTERACTION" ->
                "Interaction"

            "GRAMMAR" ->
                "Grammar"

            "DISCOURSE" ->
                "Discourse"

            "NARRATION" ->
                "Narration"

            "PRAGMATICS" ->
                "Pragmatics"

            "VOCABULARY" ->
                "Vocabulary"

            else ->
                category.toDisplayName()
        }
    }

    private fun String.toDisplayName():
            String {

        return trim()
            .lowercase()
            .replace(
                oldValue =
                    "_",

                newValue =
                    " "
            )
            .replaceFirstChar { character ->
                character.titlecase()
            }
    }

    private fun cefrRank(
        cefrLevel: String
    ): Int {

        return when (
            cefrLevel
                .trim()
                .uppercase()
        ) {
            "A1" ->
                1

            "A2" ->
                2

            "B1" ->
                3

            "B2" ->
                4

            "C1" ->
                5

            "C2" ->
                6

            else ->
                Int.MAX_VALUE
        }
    }

    private companion object {

        const val RECOMMENDED_CAPABILITY_LIMIT =
            4

        const val RECENT_CAPABILITY_LIMIT =
            5
    }
}