// app/src/main/java/com/julesmeister/deutschcraft/feature/progress/presentation/LearningFocusProgressUiModel.kt

package com.julesmeister.deutschcraft.feature.progress.presentation

import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.LearningFocusType
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.MasteryState

data class LearningFocusProgressUiModel(
    val focusKey: String,

    val title: String,

    val description: String,

    val type:
    LearningFocusType,

    val masteryState:
    MasteryState,

    val masteryPercentage: Int,

    val confidencePercentage: Int,

    val evidenceCount: Int,

    val struggleCount: Int,

    val successCount: Int,

    val consecutiveSuccessCount: Int,

    val reinforcementInstruction: String,

    val learnerGuidance: String,

    val lastObservedAt: Long,

    val lastPracticedAt: Long?,

    val masteredAt: Long?
) {

    val safeMasteryPercentage: Int
        get() =
            masteryPercentage.coerceIn(
                minimumValue =
                    0,

                maximumValue =
                    100
            )

    val safeConfidencePercentage: Int
        get() =
            confidencePercentage.coerceIn(
                minimumValue =
                    0,

                maximumValue =
                    100
            )

    val safeEvidenceCount: Int
        get() =
            evidenceCount.coerceAtLeast(
                minimumValue =
                    0
            )

    val safeStruggleCount: Int
        get() =
            struggleCount.coerceAtLeast(
                minimumValue =
                    0
            )

    val safeSuccessCount: Int
        get() =
            successCount.coerceAtLeast(
                minimumValue =
                    0
            )

    val safeConsecutiveSuccessCount: Int
        get() =
            consecutiveSuccessCount.coerceAtLeast(
                minimumValue =
                    0
            )

    val isReinforcementFocus: Boolean
        get() =
            masteryState ==
                    MasteryState.STRUGGLING ||
                    masteryState ==
                    MasteryState.PRACTICING ||
                    masteryState ==
                    MasteryState.NEEDS_REVIEW

    val isImproving: Boolean
        get() =
            masteryState ==
                    MasteryState.IMPROVING

    val isMastered: Boolean
        get() =
            masteryState ==
                    MasteryState.MASTERED
}