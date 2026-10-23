package com.julesmeister.deutschcraft.feature.chat.domain

interface ChatRepository {

    suspend fun sendMessage(
        message: String
    ): AssistantReply
}