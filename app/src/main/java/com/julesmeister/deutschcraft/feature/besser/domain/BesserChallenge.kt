// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/domain/BesserChallenge.kt

package com.julesmeister.deutschcraft.feature.besser.domain

data class BesserChallenge(
    val id: String,
    val messageId: String,
    val conversationId: String,
    val capabilityId: String,
    val outcome: BesserChallengeOutcome,
    val instruction: String,
    val reasonCode: String,
    val confidence: Float,
    val recommendedAssistanceLevel: AssistanceLevel,
    val actualAssistanceLevel: AssistanceLevel?,
    val status: ChallengeStatus,
    val hints: List<String>,
    val revealedHintCount: Int,
    val originalMessageContent: String,
    val refinedMessageContent: String?,

    val evaluationScore: Int? =
        null,

    val evaluationFeedback: String? =
        null,

    val evaluationEvidence: String? =
        null,

    val createdAt: Long,
    val acceptedAt: Long?,
    val skippedAt: Long?,
    val completedAt: Long?,
    val updatedAt: Long
) {

    val visibleHints: List<String>
        get() =
            hints.take(
                revealedHintCount.coerceIn(
                    minimumValue =
                        0,
                    maximumValue =
                        hints.size
                )
            )

    val canRevealHint: Boolean
        get() =
            status.isActive &&
                    revealedHintCount <
                    hints.size

    val canAccept: Boolean
        get() =
            status ==
                    ChallengeStatus.AVAILABLE

    val canSkip: Boolean
        get() =
            status.isActive

    val canComplete: Boolean
        get() =
            status.isActive

    val safeEvaluationScore: Int?
        get() =
            evaluationScore
                ?.coerceIn(
                    minimumValue =
                        0,
                    maximumValue =
                        100
                )

    val hasEvaluationEvidence: Boolean
        get() =
            safeEvaluationScore !=
                    null ||
                    !evaluationFeedback.isNullOrBlank() ||
                    !evaluationEvidence.isNullOrBlank()
}