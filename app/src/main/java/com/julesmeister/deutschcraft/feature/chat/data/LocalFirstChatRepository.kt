package com.julesmeister.deutschcraft.feature.chat.data

import com.julesmeister.deutschcraft.feature.chat.data.local.ChatLocalDataSource
import com.julesmeister.deutschcraft.feature.chat.data.local.toDomain
import com.julesmeister.deutschcraft.feature.chat.data.local.toEntity
import com.julesmeister.deutschcraft.feature.chat.domain.AssistantReply
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalFirstChatRepository @Inject constructor(
    private val localDataSource: ChatLocalDataSource
) : ChatRepository {

    override fun observeMessages(
        conversationId: String
    ): Flow<List<ChatMessage>> {
        return localDataSource
            .observeMessages(conversationId)
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override suspend fun getOrCreatePrimaryConversation():
            Conversation {
        val conversationId = PRIMARY_CONVERSATION_ID

        val existingConversation =
            localDataSource.getConversation(conversationId)

        if (existingConversation != null) {
            return existingConversation.toDomain()
        }

        val now = System.currentTimeMillis()

        val conversation = Conversation(
            id = conversationId,
            title = "DeutschCraft",
            currentCefrLevel = "A1",
            createdAt = now,
            updatedAt = now,
            lastMessagePreview = null,
            isArchived = false
        )

        localDataSource.createConversation(
            conversation.toEntity()
        )

        val greeting = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversation.id,
            content = "Hallo! Wie war dein Tag?",
            sender = MessageSender.ASSISTANT,
            sequenceNumber = 1L,
            timestamp = now
        )

        saveMessage(greeting)

        return conversation
    }

    override suspend fun saveMessage(
        message: ChatMessage
    ) {
        localDataSource.saveMessage(
            message.toEntity()
        )
    }

    override suspend fun getNextSequenceNumber(
        conversationId: String
    ): Long {
        return localDataSource.getNextSequenceNumber(
            conversationId
        )
    }

    override suspend fun generateAssistantReply(
        conversationId: String,
        message: String
    ): AssistantReply {
        delay(1_200)

        val sequenceNumber =
            getNextSequenceNumber(conversationId)

        return AssistantReply(
            message = ChatMessage(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                content =
                    "Das klingt interessant! " +
                            "Wie fühlst du dich dabei?",
                sender = MessageSender.ASSISTANT,
                sequenceNumber = sequenceNumber,
                timestamp = System.currentTimeMillis()
            ),
            suggestions = listOf(
                ReplySuggestion(
                    id = UUID.randomUUID().toString(),
                    text = "Ich fühle mich sehr gut."
                ),
                ReplySuggestion(
                    id = UUID.randomUUID().toString(),
                    text = "Ich bin ein bisschen nervös."
                ),
                ReplySuggestion(
                    id = UUID.randomUUID().toString(),
                    text = "Ich weiß es noch nicht."
                )
            )
        )
    }

    private companion object {
        const val PRIMARY_CONVERSATION_ID =
            "primary-conversation"
    }
}