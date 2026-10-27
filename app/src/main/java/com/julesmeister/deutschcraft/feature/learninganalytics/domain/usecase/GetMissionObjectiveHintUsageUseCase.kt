package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionObjectiveHintUsage
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionObjectiveHintUsageRepository
import javax.inject.Inject

class GetMissionObjectiveHintUsageUseCase
@Inject constructor(
    private val repository:
    MissionObjectiveHintUsageRepository
) {

    suspend operator fun invoke(
        conversationMissionId: String
    ): Map<String, ObjectiveHintUsageSummary> {
        return repository
            .getMissionHintUsage(
                conversationMissionId =
                    conversationMissionId
            )
            .groupBy { usage ->
                usage.objectiveId
            }
            .mapValues { entry ->
                entry.value.toSummary()
            }
    }

    private fun List<MissionObjectiveHintUsage>.toSummary():
            ObjectiveHintUsageSummary {
        return ObjectiveHintUsageSummary(
            objectiveId =
                first().objectiveId,
            hintsUsed =
                size.coerceIn(
                    minimumValue =
                        0,
                    maximumValue =
                        MAXIMUM_HINT_COUNT
                ),
            highestHintLevel =
                maxOfOrNull { usage ->
                    usage.hintLevel
                }
                    ?.coerceIn(
                        minimumValue =
                            0,
                        maximumValue =
                            MAXIMUM_HINT_COUNT
                    )
                    ?: 0,
            revealedHintLevels =
                map { usage ->
                    usage.hintLevel
                }
                    .distinct()
                    .sorted(),
            firstHintRevealedAt =
                minOfOrNull { usage ->
                    usage.revealedAt
                },
            lastHintRevealedAt =
                maxOfOrNull { usage ->
                    usage.revealedAt
                }
        )
    }

    private companion object {
        const val MAXIMUM_HINT_COUNT =
            3
    }
}

data class ObjectiveHintUsageSummary(
    val objectiveId: String,
    val hintsUsed: Int,
    val highestHintLevel: Int,
    val revealedHintLevels: List<Int>,
    val firstHintRevealedAt: Long?,
    val lastHintRevealedAt: Long?
)