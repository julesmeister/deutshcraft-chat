package com.julesmeister.deutschcraft.feature.chat.data.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QwenChatCompletionResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<QwenChoice> = emptyList(),
    val usage: QwenUsage? = null
)

@Serializable
data class QwenChoice(
    val index: Int = 0,
    val message: QwenResponseMessage,

    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class QwenResponseMessage(
    val role: String? = null,
    val content: String? = null
)

@Serializable
data class QwenUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,

    @SerialName("completion_tokens")
    val completionTokens: Int = 0,

    @SerialName("total_tokens")
    val totalTokens: Int = 0,

    @SerialName("prompt_tokens_details")
    val promptTokenDetails:
    QwenPromptTokenDetails? = null
)

@Serializable
data class QwenPromptTokenDetails(
    @SerialName("cached_tokens")
    val cachedTokens: Int = 0
)