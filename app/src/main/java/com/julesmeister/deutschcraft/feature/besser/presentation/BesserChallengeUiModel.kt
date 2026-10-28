package com.julesmeister.deutschcraft.feature.besser.presentation

import com.julesmeister.deutschcraft.feature.besser.domain.AssistanceLevel
import com.julesmeister.deutschcraft.feature.besser.domain.ChallengeStatus

data class BesserChallengeUiModel(
    val id: String,
    val messageId: String,
    val conversationId: String,
    val capabilityId: String,
    val instruction: String,
    val confidence: Float,
    val recommendedAssistanceLevel: AssistanceLevel,
    val status: ChallengeStatus,
    val hints: List<String>,
    val revealedHintCount: Int,
    val originalMessageContent: String
) {

    val visibleHints: List<String>
        get() =
            hints.take(
                revealedHintCount.coerceIn(
                    minimumValue = 0,
                    maximumValue = hints.size
                )
            )

    val canImprove: Boolean
        get() =
            status.isActive

    val canRevealHint: Boolean
        get() =
            status.isActive &&
                    revealedHintCount < hints.size

    val canSkip: Boolean
        get() =
            status.isActive
}