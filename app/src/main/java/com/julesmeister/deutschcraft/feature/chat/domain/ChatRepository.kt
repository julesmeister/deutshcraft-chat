package com.julesmeister.deutschcraft.feature.chat.domain

import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun observeMessages(
        conversationId: String
    ): Flow<List<ChatMessage>>

    suspend fun getOrCreatePrimaryConversation():
            Conversation

    suspend fun saveMessage(
        message: ChatMessage
    )

    suspend fun getNextSequenceNumber(
        conversationId: String
    ): Long

    suspend fun generateAssistantReply(
        conversationId: String,
        message: String
    ): AssistantReply
}