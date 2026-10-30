// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/domain/mastery/MissionCapabilityEvidence.kt

package com.julesmeister.deutschcraft.feature.besser.domain.mastery

data class MissionCapabilityEvidence(
    val missionId: String,

    val conversationId: String,

    val capabilityId: String,

    val overallScore: Int,

    val independenceScore: Int,

    val objectiveCompletionPercentage: Int,

    val evidenceScore: Int,

    val confidence: Float,

    val demonstrated: Boolean,

    val evaluatedAt: Long
) {

    val safeOverallScore: Int
        get() =
            overallScore.coerceIn(
                minimumValue =
                    0,

                maximumValue =
                    100
            )

    val safeIndependenceScore: Int
        get() =
            independenceScore.coerceIn(
                minimumValue =
                    0,

                maximumValue =
                    100
            )

    val safeObjectiveCompletionPercentage: Int
        get() =
            objectiveCompletionPercentage.coerceIn(
                minimumValue =
                    0,

                maximumValue =
                    100
            )

    val safeEvidenceScore: Int
        get() =
            evidenceScore.coerceIn(
                minimumValue =
                    0,

                maximumValue =
                    100
            )

    val safeConfidence: Float
        get() =
            confidence.coerceIn(
                minimumValue =
                    0f,

                maximumValue =
                    1f
            )

    val weightedEvidenceValue: Float
        get() =
            (
                    safeEvidenceScore.toFloat() /
                            100f
                    ) *
                    safeConfidence
}