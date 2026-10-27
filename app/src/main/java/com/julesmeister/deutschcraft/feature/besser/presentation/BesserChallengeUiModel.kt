package com.julesmeister.deutschcraft.feature.besser.presentation

import com.julesmeister.deutschcraft.feature.besser.domain.AssistanceLevel

data class BesserChallengeUiModel(
    val id: String,
    val messageId: String,
    val capabilityId: String,
    val instruction: String,
    val confidence: Float,
    val assistanceLevel: AssistanceLevel,
    val hints: List<String>,
    val revealedHintCount: Int = 0
) {
    val visibleHints: List<String>
        get() =
            hints.take(
                revealedHintCount
            )

    val canRevealHint: Boolean
        get() =
            revealedHintCount <
                    hints.size
}