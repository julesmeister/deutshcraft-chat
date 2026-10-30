package com.julesmeister.deutschcraft.feature.besser.domain.mastery

enum class CapabilityMasteryStatus {
    DEVELOPING,
    PRACTISING,
    MASTERED
}

data class CapabilityMastery(
    val capabilityId: String,
    val attemptCount: Int,
    val completedCount: Int,
    val skippedCount: Int,
    val masteryScore: Float,
    val status: CapabilityMasteryStatus,
    val lastChallengedAt: Long?
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

    val hasHistory: Boolean
        get() =
            attemptCount >
                    0

    companion object {

        fun empty(
            capabilityId: String
        ): CapabilityMastery {
            return CapabilityMastery(
                capabilityId =
                    capabilityId,

                attemptCount =
                    0,

                completedCount =
                    0,

                skippedCount =
                    0,

                masteryScore =
                    0f,

                status =
                    CapabilityMasteryStatus.DEVELOPING,

                lastChallengedAt =
                    null
            )
        }
    }
}