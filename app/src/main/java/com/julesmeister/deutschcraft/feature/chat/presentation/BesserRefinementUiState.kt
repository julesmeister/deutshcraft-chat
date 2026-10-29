package com.julesmeister.deutschcraft.feature.chat.presentation

data class BesserRefinementUiState(
    val challengeId: String,
    val sourceMessageId: String,
    val originalMessageContent: String,
    val instruction: String,
    val capabilityId: String,
    val revealedHintCount: Int
)