package com.julesmeister.deutschcraft.feature.besser.presentation

data class BesserChallengeUiModel(
    val id: String,
    val messageId: String,
    val capabilityId: String,
    val capabilityName: String,
    val instruction: String,
    val rationale: String,
    val hints: List<String>,
    val revealedHintCount: Int,
    val confidence: Float,
    val isRefinementActive: Boolean
) {

    /*
     * Canonical name used by the newer session architecture.
     */
    val revealedHints: List<String>
        get() =
            hints.take(
                revealedHintCount.coerceIn(
                    minimumValue =
                        0,
                    maximumValue =
                        hints.size
                )
            )

    /*
     * Compatibility name used by BesserChallengeBubble.
     */
    val visibleHints: List<String>
        get() =
            revealedHints

    val canRevealAnotherHint: Boolean
        get() =
            revealedHintCount <
                    hints.size

    /*
     * Compatibility name used by BesserChallengeBubble.
     */
    val canRevealHint: Boolean
        get() =
            canRevealAnotherHint

    fun revealNextHint():
            BesserChallengeUiModel {
        if (!canRevealAnotherHint) {
            return this
        }

        return copy(
            revealedHintCount =
                revealedHintCount + 1
        )
    }

    fun startRefinement():
            BesserChallengeUiModel {
        if (isRefinementActive) {
            return this
        }

        return copy(
            isRefinementActive =
                true
        )
    }

    fun stopRefinement():
            BesserChallengeUiModel {
        if (!isRefinementActive) {
            return this
        }

        return copy(
            isRefinementActive =
                false
        )
    }
}