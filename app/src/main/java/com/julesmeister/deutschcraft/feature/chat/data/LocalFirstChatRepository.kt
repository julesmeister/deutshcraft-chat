package com.julesmeister.deutschcraft.feature.chat.data

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.database.entity.ConversationTitleSource
import com.julesmeister.deutschcraft.feature.chat.data.local.ChatLocalDataSource
import com.julesmeister.deutschcraft.feature.chat.data.local.toDomain
import com.julesmeister.deutschcraft.feature.chat.data.local.toEntity
import com.julesmeister.deutschcraft.feature.chat.domain.AssistantReply
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalFirstChatRepository @Inject constructor(
    private val localDataSource: ChatLocalDataSource,
    private val clock: Clock
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
        return createConversation(
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

        /*
         * Only the temporary primary-conversation route
         * may create a conversation when it is missing.
         *
         * A random or stale ID must not silently create
         * an unrelated thread.
         */
        require(
            conversationId ==
                    PRIMARY_CONVERSATION_ID
        ) {
            "Conversation $conversationId does not exist." }

        return createConversation(
            conversationId = conversationId
        )
    }

    override suspend fun getOrCreatePrimaryConversation():
            Conversation {
        val conversationId =
            PRIMARY_CONVERSATION_ID

        val existingConversation =
            localDataSource.getConversation(
                conversationId = conversationId
            )

        if (existingConversation != null) {
            return existingConversation.toDomain()
        }

        return createConversation(
            conversationId = conversationId
        )
    }

    override suspend fun saveMessage(
        message: ChatMessage
    ) {
        localDataSource.saveMessage(
            message = message.toEntity()
        )
    }

    override suspend fun getNextSequenceNumber(
        conversationId: String
    ): Long {
        return localDataSource
            .getNextSequenceNumber(
                conversationId = conversationId
            )
    }

    override suspend fun generateAssistantReply(
        conversationId: String,
        message: String
    ): AssistantReply {
        /*
         * Temporary delay used to simulate an
         * assistant generating a response.
         *
         * This will later be replaced by the
         * Spring Boot and Qwen request.
         */
        delay(ASSISTANT_REPLY_DELAY_MILLIS)

        val sequenceNumber =
            getNextSequenceNumber(
                conversationId = conversationId
            )

        val currentTimestamp =
            clock.instant().toEpochMilli()

        return AssistantReply(
            message = ChatMessage(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                content = createFakeAssistantResponse(
                    userMessage = message
                ),
                sender = MessageSender.ASSISTANT,
                sequenceNumber = sequenceNumber,
                timestamp = currentTimestamp
            ),
            suggestions =
                createFakeReplySuggestions()
        )
    }

    private suspend fun createConversation(
        conversationId: String
    ): Conversation {
        val currentInstant =
            clock.instant()

        val timestamp =
            currentInstant.toEpochMilli()

        val localDate =
            currentInstant
                .atZone(clock.zone)
                .toLocalDate()

        val conversation = Conversation(
            id = conversationId,
            title = DEFAULT_CONVERSATION_TITLE,
            titleSource =
                ConversationTitleSource.DEFAULT,
            cefrLevel = CefrLevel.A1,
            localDate = localDate.toString(),
            epochDay = localDate.toEpochDay(),
            createdAt = timestamp,
            updatedAt = timestamp,
            closedAt = null,
            messageCount = 0,
            previewText = null,
            isClosed = false,
            isArchived = false
        )

        localDataSource.createConversation(
            conversation = conversation.toEntity()
        )

        val greeting = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversation.id,
            content = INITIAL_GREETING,
            sender = MessageSender.ASSISTANT,
            sequenceNumber =
                INITIAL_MESSAGE_SEQUENCE_NUMBER,
            timestamp = timestamp
        )

        saveMessage(
            message = greeting
        )

        /*
         * Fetch the conversation again because saving
         * the greeting updates its message count,
         * preview text, and updated timestamp.
         */
        return getConversation(
            conversationId = conversation.id
        ) ?: conversation
    }

    private fun createFakeAssistantResponse(
        userMessage: String
    ): String {
        /*
         * The parameter is intentionally referenced
         * so the fake implementation can later become
         * slightly context-sensitive.
         */
        return if (userMessage.isBlank()) {
            DEFAULT_ASSISTANT_RESPONSE
        } else {
            DEFAULT_ASSISTANT_RESPONSE
        }
    }

    private fun createFakeReplySuggestions():
            List<ReplySuggestion> {
        return listOf(
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
    }

    private companion object {
        const val PRIMARY_CONVERSATION_ID =
            "primary-conversation"

        const val DEFAULT_CONVERSATION_TITLE =
            "Neue Unterhaltung"

        const val INITIAL_GREETING =
            "Hallo! Wie war dein Tag?"

        const val DEFAULT_ASSISTANT_RESPONSE =
            "Das klingt interessant! " +
                    "Wie fühlst du dich dabei?"

        const val INITIAL_MESSAGE_SEQUENCE_NUMBER =
            1L

        const val ASSISTANT_REPLY_DELAY_MILLIS =
            1_200L
    }

    override suspend fun getOpenConversationForToday():
            Conversation? {
        val todayEpochDay = clock
            .instant()
            .atZone(clock.zone)
            .toLocalDate()
            .toEpochDay()

        return localDataSource
            .getOpenConversationForDay(
                epochDay = todayEpochDay
            )
            ?.toDomain()
    }
}