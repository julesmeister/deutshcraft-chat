package com.julesmeister.deutschcraft.feature.chat.data.ai

import kotlinx.serialization.Serializable

@Serializable
data class QwenSuggestionPayload(
    val text: String = ""
)