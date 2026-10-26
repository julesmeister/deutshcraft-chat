package com.julesmeister.deutschcraft.feature.chat.data

import com.julesmeister.deutschcraft.feature.chat.data.factory.AssistantReplyFactory
import com.julesmeister.deutschcraft.feature.chat.data.factory.ConversationFactory
import com.julesmeister.deutschcraft.feature.chat.data.local.ChatLocalDataSource
import com.julesmeister.deutschcraft.feature.chat.data.local.toDomain
import com.julesmeister.deutschcraft.feature.chat.data.local.toEntity
import com.julesmeister.deutschcraft.feature.chat.domain.AssistantReply
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.threads.domain.LocalThreadTitleGenerator
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalFirstChatRepository @Inject constructor(
    private val localDataSource:
    ChatLocalDataSource,
    private val conversationFactory:
    ConversationFactory,
    private val assistantReplyFactory:
    AssistantReplyFactory,
    private val titleGenerator:
    LocalThreadTitleGenerator,
    private val clock:
    Clock
) : ChatRepository {

    override fun observeMessages(
        conversationId: String
    ): Flow<List<ChatMessage>> {
        return localDataSource
            .observeMessages(
                conversationId = conversationId
            )
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override suspend fun createConversation():
            Conversation {
        return conversationFactory.create(
            conversationId =
                UUID.randomUUID().toString()
        )
    }

    override suspend fun getConversation(
        conversationId: String
    ): Conversation? {
        return localDataSource
            .getConversation(
                conversationId = conversationId
            )
            ?.toDomain()
    }

    override suspend fun getOrCreateConversation(
        conversationId: String
    ): Conversation {
        val existingConversation =
            getConversation(
                conversationId = conversationId
            )

        if (existingConversation != null) {
            return existingConversation
        }

        require(
            conversationId ==
                    PRIMARY_CONVERSATION_ID
        ) {
            "Conversation $conversationId does not exist."
        }

        return conversationFactory.create(
            conversationId = conversationId
        )
    }

    override suspend fun getOrCreatePrimaryConversation():
            Conversation {
        val existingConversation =
            getConversation(
                conversationId =
                    PRIMARY_CONVERSATION_ID
            )

        if (existingConversation != null) {
            return existingConversation
        }

        return conversationFactory.create(
            conversationId =
                PRIMARY_CONVERSATION_ID
        )
    }

    override suspend fun getOpenConversationForToday():
            Conversation? {
        val todayEpochDay =
            clock
                .instant()
                .atZone(
                    clock.zone
                )
                .toLocalDate()
                .toEpochDay()

        return localDataSource
            .getOpenConversationForDay(
                epochDay = todayEpochDay
            )
            ?.toDomain()
    }

    override suspend fun saveMessage(
        message: ChatMessage
    ) {
        localDataSource.saveMessage(
            message =
                message.toEntity()
        )

        if (
            message.sender ==
            MessageSender.USER
        ) {
            updateGeneratedTitleIfNeeded(
                message = message
            )
        }
    }

    override suspend fun getNextSequenceNumber(
        conversationId: String
    ): Long {
        return localDataSource.getNextSequenceNumber(
            conversationId = conversationId
        )
    }

    override suspend fun generateAssistantReply(
        conversationId: String,
        message: String,
        activeMission: ConversationMission?
    ): AssistantReply {
        return assistantReplyFactory.create(
            conversationId =
                conversationId,
            learnerMessage =
                message,
            activeMission =
                activeMission
        )
    }

    override suspend fun updateMessageCorrection(
        messageId: String,
        correction: String?
    ) {
        localDataSource.updateMessageCorrection(
            messageId =
                messageId,
            correction =
                correction
        )
    }

    private suspend fun updateGeneratedTitleIfNeeded(
        message: ChatMessage
    ) {
        if (message.content.isBlank()) {
            return
        }

        val generatedTitle =
            titleGenerator.generate(
                message =
                    message.content
            )

        localDataSource
            .updateGeneratedConversationTitle(
                conversationId =
                    message.conversationId,
                title =
                    generatedTitle,
                updatedAt =
                    clock.millis()
            )
    }

    private companion object {
        const val PRIMARY_CONVERSATION_ID =
            "primary-conversation"
    }
}