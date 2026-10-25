package com.julesmeister.deutschcraft.feature.chat.data.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QwenChatCompletionRequest(
    val model: String,
    val messages: List<QwenMessage>,

    @SerialName("enable_thinking")
    val enableThinking: Boolean = false,

    val temperature: Double = 0.7,

    @SerialName("max_tokens")
    val maxTokens: Int = 300,

    @SerialName("response_format")
    val responseFormat:
    QwenResponseFormat? = null
)

@Serializable
data class QwenResponseFormat(
    val type: String
)