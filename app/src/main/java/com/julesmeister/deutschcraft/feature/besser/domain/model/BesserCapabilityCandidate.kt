// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/domain/model/BesserCapabilityCandidate.kt

package com.julesmeister.deutschcraft.feature.besser.domain.model

import com.julesmeister.deutschcraft.feature.besser.domain.mastery.CapabilityMasteryStatus

data class BesserCapabilityCandidate(
    val capabilityId: String,
    val name: String,
    val description: String,
    val category: String,
    val cefrLevel: String,

    val localPriorityScore: Float,

    val lexicalMatchCount: Int,
    val categoryMatched: Boolean,
    val isTargetLevelCapability: Boolean,
    val sortOrder: Int,

    val masteryScore: Float =
        0f,

    val masteryStatus:
    CapabilityMasteryStatus =
        CapabilityMasteryStatus.DEVELOPING,

    val masteryAttemptCount: Int =
        0,

    val masteryCompletedCount: Int =
        0,

    val masterySkippedCount: Int =
        0,

    val lastChallengedAt: Long? =
        null,

    /*
     * =======================================================
     * PROGRESSION
     * =======================================================
     */

    val progressionReady: Boolean =
        true,

    val progressionPrerequisiteCount: Int =
        0,

    val satisfiedProgressionPrerequisiteCount: Int =
        0,

    val averagePrerequisiteMastery: Float =
        1f,

    val isDirectSuccessorOfMasteredCapability: Boolean =
        false
) {

    val safeMasteryScore: Float
        get() =
            masteryScore.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            )

    val masteryPercentage: Int
        get() =
            (
                    safeMasteryScore *
                            100f
                    )
                .toInt()
                .coerceIn(
                    minimumValue =
                        0,
                    maximumValue =
                        100
                )

    val hasMasteryHistory: Boolean
        get() =
            masteryAttemptCount >
                    0

    val safeAveragePrerequisiteMastery: Float
        get() =
            averagePrerequisiteMastery.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            )

    val prerequisiteMasteryPercentage: Int
        get() =
            (
                    safeAveragePrerequisiteMastery *
                            100f
                    )
                .toInt()
                .coerceIn(
                    minimumValue =
                        0,
                    maximumValue =
                        100
                )
}