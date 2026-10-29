package com.julesmeister.deutschcraft.feature.chat.domain

import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun observeMessages(
        conversationId: String
    ): Flow<List<ChatMessage>>

    suspend fun getConversation(
        conversationId: String
    ): Conversation?

    suspend fun getOrCreateConversation(
        conversationId: String
    ): Conversation

    suspend fun getOrCreatePrimaryConversation():
            Conversation

    suspend fun getOpenConversationForToday():
            Conversation?

    suspend fun createConversation():
            Conversation

    /*
     * Creates a conversation using a caller-supplied ID.
     *
     * This is used when a provisional new chat receives its
     * first real learner message.
     */
    suspend fun createConversation(
        conversationId: String
    ): Conversation

    suspend fun saveMessage(
        message: ChatMessage
    )

    suspend fun getNextSequenceNumber(
        conversationId: String
    ): Long

    suspend fun generateAssistantReply(
        conversationId: String,
        message: String,
        activeMission:
        ConversationMission? =
            null
    ): AssistantReply

    suspend fun updateMessageCorrection(
        messageId: String,
        correction: String?
    )
}