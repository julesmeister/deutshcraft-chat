package com.julesmeister.deutschcraft.feature.chat.data.factory

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.database.entity.ConversationTitleSource
import com.julesmeister.deutschcraft.feature.chat.data.local.ChatLocalDataSource
import com.julesmeister.deutschcraft.feature.chat.data.local.toDomain
import com.julesmeister.deutschcraft.feature.chat.data.local.toEntity
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import java.time.Clock
import java.util.UUID
import javax.inject.Inject

class ConversationFactory @Inject constructor(
    private val localDataSource:
    ChatLocalDataSource,
    private val clock:
    Clock
) {

    suspend fun create(
        conversationId: String,
        title: String =
            DEFAULT_CONVERSATION_TITLE,
        titleSource: ConversationTitleSource =
            ConversationTitleSource.DEFAULT,
        cefrLevel: CefrLevel =
            CefrLevel.A1,
        openingMessage: String =
            INITIAL_GREETING
    ): Conversation {
        val currentInstant =
            clock.instant()

        val timestamp =
            currentInstant.toEpochMilli()

        val localDate =
            currentInstant
                .atZone(
                    clock.zone
                )
                .toLocalDate()

        val cleanTitle =
            title
                .trim()
                .ifBlank {
                    DEFAULT_CONVERSATION_TITLE
                }

        val cleanOpeningMessage =
            openingMessage
                .trim()
                .ifBlank {
                    INITIAL_GREETING
                }

        val conversation =
            Conversation(
                id =
                    conversationId,
                title =
                    cleanTitle,
                titleSource =
                    titleSource,
                cefrLevel =
                    cefrLevel,
                localDate =
                    localDate.toString(),
                epochDay =
                    localDate.toEpochDay(),
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp,
                closedAt =
                    null,
                messageCount =
                    0,
                previewText =
                    null,
                isClosed =
                    false,
                isArchived =
                    false
            )

        localDataSource.createConversation(
            conversation =
                conversation.toEntity()
        )

        val greeting =
            ChatMessage(
                id =
                    UUID.randomUUID().toString(),
                conversationId =
                    conversation.id,
                content =
                    cleanOpeningMessage,
                sender =
                    MessageSender.ASSISTANT,
                sequenceNumber =
                    INITIAL_MESSAGE_SEQUENCE_NUMBER,
                timestamp =
                    timestamp
            )

        localDataSource.saveMessage(
            message =
                greeting.toEntity()
        )

        /*
         * Saving the opening message changes:
         *
         * - Preview text
         * - Message count
         * - Updated timestamp
         */
        return localDataSource
            .getConversation(
                conversationId =
                    conversation.id
            )
            ?.toDomain()
            ?: conversation
    }

    private companion object {
        const val DEFAULT_CONVERSATION_TITLE =
            "Neue Unterhaltung"

        const val INITIAL_GREETING =
            "Hallo! Wie war dein Tag?"

        const val INITIAL_MESSAGE_SEQUENCE_NUMBER =
            1L
    }
}