package com.julesmeister.deutschcraft.feature.chat.data.ai

import kotlinx.serialization.Serializable

@Serializable
data class QwenReplyPayload(
    val reply: String = "",
    val suggestions: List<String> = emptyList()
)