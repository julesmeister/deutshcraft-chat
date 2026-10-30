// app/src/main/java/com/julesmeister/deutschcraft/feature/progress/presentation/BesserProgressUiModel.kt

package com.julesmeister.deutschcraft.feature.progress.presentation

import com.julesmeister.deutschcraft.feature.besser.domain.mastery.CapabilityMasteryStatus

data class BesserProgressUiModel(
    val capabilityCount: Int =
        0,

    val attemptedCapabilityCount: Int =
        0,

    val completedAttemptCount: Int =
        0,

    val masteredCapabilityCount: Int =
        0,

    val practisingCapabilityCount: Int =
        0,

    val developingCapabilityCount: Int =
        0,

    val overallMasteryPercentage: Int =
        0,

    val categories:
    List<BesserCategoryProgressUiModel> =
        emptyList(),

    val recommendedNextCapabilities:
    List<BesserCapabilityProgressUiModel> =
        emptyList(),

    val recentCapabilities:
    List<BesserCapabilityProgressUiModel> =
        emptyList()
) {

    val hasEvidence: Boolean
        get() =
            attemptedCapabilityCount >
                    0

    val hasRecommendations: Boolean
        get() =
            recommendedNextCapabilities
                .isNotEmpty()

    val safeOverallMasteryPercentage: Int
        get() =
            overallMasteryPercentage.coerceIn(
                minimumValue =
                    0,

                maximumValue =
                    100
            )
}

data class BesserCategoryProgressUiModel(
    val category: String,

    val displayNameGerman: String,

    val displayNameEnglish: String,

    val masteryPercentage: Int,

    val attemptedCapabilityCount: Int,

    val capabilityCount: Int,

    val masteredCapabilityCount: Int
) {

    val safeMasteryPercentage: Int
        get() =
            masteryPercentage.coerceIn(
                minimumValue =
                    0,

                maximumValue =
                    100
            )
}

data class BesserCapabilityProgressUiModel(
    val capabilityId: String,

    val name: String,

    val description: String,

    val category: String,

    val cefrLevel: String,

    val masteryPercentage: Int,

    val masteryStatus:
    CapabilityMasteryStatus,

    val attemptCount: Int,

    val completedCount: Int,

    val skippedCount: Int,

    val progressionReady: Boolean,

    val isDirectSuccessorOfMasteredCapability: Boolean,

    val prerequisiteMasteryPercentage: Int,

    val satisfiedPrerequisiteCount: Int,

    val prerequisiteCount: Int,

    val lastChallengedAt: Long?
) {

    val safeMasteryPercentage: Int
        get() =
            masteryPercentage.coerceIn(
                minimumValue =
                    0,

                maximumValue =
                    100
            )

    val safePrerequisiteMasteryPercentage: Int
        get() =
            prerequisiteMasteryPercentage.coerceIn(
                minimumValue =
                    0,

                maximumValue =
                    100
            )

    val hasHistory: Boolean
        get() =
            attemptCount >
                    0

    val hasPrerequisites: Boolean
        get() =
            prerequisiteCount >
                    0
}