// app/src/main/java/com/julesmeister/deutschcraft/feature/progress/presentation/ProgressUiState.kt

package com.julesmeister.deutschcraft.feature.progress.presentation

import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSummary

data class ProgressUiState(
    val progress:
    ProgressSummary? =
        null,

    val completedMissions:
    List<CompletedMissionUiModel> =
        emptyList(),

    val besserProgress:
    BesserProgressUiModel? =
        null,

    val learningFocuses:
    List<LearningFocusProgressUiModel> =
        emptyList(),

    val isProgressLoading: Boolean =
        true,

    val areMissionsLoading: Boolean =
        true,

    val isBesserProgressLoading: Boolean =
        true,

    val areLearningFocusesLoading: Boolean =
        true,

    val progressErrorMessage: String? =
        null,

    val missionErrorMessage: String? =
        null,

    val besserProgressErrorMessage: String? =
        null,

    val learningFocusErrorMessage: String? =
        null
) {

    val completedMissionCount: Int
        get() =
            completedMissions.size

    val recentCompletedMissions:
            List<CompletedMissionUiModel>
        get() =
            completedMissions.take(
                RECENT_MISSION_LIMIT
            )

    val prioritizedLearningFocuses:
            List<LearningFocusProgressUiModel>
        get() =
            learningFocuses
                .sortedWith(
                    compareBy<LearningFocusProgressUiModel> { focus ->
                        focus.masteryState.priority
                    }
                        .thenByDescending { focus ->
                            focus.lastObservedAt
                        }
                        .thenByDescending { focus ->
                            focus.safeConfidencePercentage
                        }
                )
                .take(
                    LEARNING_FOCUS_DISPLAY_LIMIT
                )

    val reinforcementFocusCount: Int
        get() =
            learningFocuses.count { focus ->
                focus.isReinforcementFocus
            }

    val improvingFocusCount: Int
        get() =
            learningFocuses.count { focus ->
                focus.isImproving
            }

    val masteredFocusCount: Int
        get() =
            learningFocuses.count { focus ->
                focus.isMastered
            }

    val isLoading: Boolean
        get() =
            isProgressLoading ||
                    areMissionsLoading ||
                    isBesserProgressLoading ||
                    areLearningFocusesLoading

    val errorMessage: String?
        get() =
            progressErrorMessage
                ?: missionErrorMessage
                ?: besserProgressErrorMessage
                ?: learningFocusErrorMessage

    val hasProgressData: Boolean
        get() =
            progress !=
                    null

    val hasCompletedMissions: Boolean
        get() =
            completedMissions.isNotEmpty()

    val hasBesserProgress: Boolean
        get() =
            besserProgress !=
                    null

    val hasLearningFocuses: Boolean
        get() =
            learningFocuses.isNotEmpty()

    val hasAnyData: Boolean
        get() =
            hasProgressData ||
                    hasCompletedMissions ||
                    hasBesserProgress ||
                    hasLearningFocuses

    private companion object {

        const val RECENT_MISSION_LIMIT =
            5

        const val LEARNING_FOCUS_DISPLAY_LIMIT =
            5
    }
}

private val com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.MasteryState.priority:
        Int
    get() =
        when (this) {
            com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.MasteryState.NEEDS_REVIEW ->
                0

            com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.MasteryState.STRUGGLING ->
                1

            com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.MasteryState.PRACTICING ->
                2

            com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.MasteryState.IMPROVING ->
                3

            com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.MasteryState.OBSERVING ->
                4

            com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.MasteryState.MASTERED ->
                5
        }