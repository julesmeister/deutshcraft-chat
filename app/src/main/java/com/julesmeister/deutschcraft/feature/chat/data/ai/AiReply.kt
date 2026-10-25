package com.julesmeister.deutschcraft.feature.chat.data.ai

data class AiReply(
    val content: String,
    val suggestions: List<String> =
        emptyList()
)