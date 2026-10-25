package com.julesmeister.deutschcraft.feature.chat.data.ai

import retrofit2.http.Body
import retrofit2.http.POST

interface QwenApi {

    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: QwenChatCompletionRequest
    ): QwenChatCompletionResponse
}