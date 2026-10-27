package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionObjectiveHintUsageRepository
import javax.inject.Inject

/*
 * Records a coaching hint when it becomes visible to the
 * learner.
 *
 * The UI uses zero-based hint indices:
 *
 * 0 = first hint
 * 1 = second hint
 * 2 = third hint
 *
 * Analytics uses one-based support levels:
 *
 * 1 = gentle support
 * 2 = more specific contextual support
 * 3 = direct wording support
 */
class RecordMissionObjectiveHintUseCase
@Inject constructor(
    private val repository:
    MissionObjectiveHintUsageRepository
) {

    /*
     * Returns true when a new hint reveal was recorded.
     *
     * Returns false when the same hint level was already
     * recorded for that mission objective.
     */
    suspend operator fun invoke(
        conversationMissionId: String,
        objectiveId: String,
        hintIndex: Int,
        hintText: String
    ): Boolean {
        require(
            conversationMissionId.isNotBlank()
        ) {
            "Conversation mission ID must not be blank."
        }

        require(
            objectiveId.isNotBlank()
        ) {
            "Objective ID must not be blank."
        }

        require(
            hintIndex in
                    MINIMUM_HINT_INDEX..
                    MAXIMUM_HINT_INDEX
        ) {
            "Hint index must be between 0 and 2."
        }

        require(
            hintText.isNotBlank()
        ) {
            "Hint text must not be blank."
        }

        return repository.recordHint(
            conversationMissionId =
                conversationMissionId,
            objectiveId =
                objectiveId,
            hintLevel =
                hintIndex +
                        HINT_LEVEL_OFFSET,
            hintText =
                hintText.trim()
        )
    }

    private companion object {
        const val MINIMUM_HINT_INDEX =
            0

        const val MAXIMUM_HINT_INDEX =
            2

        const val HINT_LEVEL_OFFSET =
            1
    }
}