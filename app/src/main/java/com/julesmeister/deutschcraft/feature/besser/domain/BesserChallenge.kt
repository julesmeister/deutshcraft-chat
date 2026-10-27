package com.julesmeister.deutschcraft.feature.besser.domain

data class BesserChallenge(
    val id: String,
    val messageId: String,
    val capabilityId: String,
    val outcome: BesserChallengeOutcome,
    val instruction: String,
    val reasonCode: String,
    val confidence: Float,
    val assistanceLevel: AssistanceLevel,
    val hints: List<String> = emptyList(),
    val relevantChallengeWordIds: List<String> = emptyList(),
    val status: BesserChallengeStatus =
        BesserChallengeStatus.AVAILABLE
)

enum class BesserChallengeStatus {
    AVAILABLE,
    ACCEPTED,
    SKIPPED,
    COMPLETED
}