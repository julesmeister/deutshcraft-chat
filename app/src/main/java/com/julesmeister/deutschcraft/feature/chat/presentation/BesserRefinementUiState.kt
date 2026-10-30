package com.julesmeister.deutschcraft.feature.chat.presentation

data class BesserRefinementUiState(
    val challengeId: String,
    val sourceMessageId: String,
    val originalMessageContent: String,
    val instruction: String,
    val capabilityId: String,
    val revealedHintCount: Int,

    /*
     * Short German feedback from the last evaluated attempt.
     */
    val feedback: String? =
        null,

    /*
     * Score from the last evaluated attempt.
     */
    val lastScore: Int? =
        null
)
