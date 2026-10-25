package com.julesmeister.deutschcraft.feature.chat.data.ai

import kotlinx.serialization.Serializable

@Serializable
data class QwenMessage(
    val role: String,
    val content: String
)
