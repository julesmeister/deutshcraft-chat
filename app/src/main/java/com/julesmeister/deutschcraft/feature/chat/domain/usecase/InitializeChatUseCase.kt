package com.julesmeister.deutschcraft.feature.chat.domain.usecase

import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.progress.domain.ProgressRepository
import java.time.Clock
import javax.inject.Inject

class InitializeChatUseCase @Inject constructor(
    private val chatRepository:
    ChatRepository,

    private val progressRepository:
    ProgressRepository,

    private val clock:
    Clock
) {

    suspend operator fun invoke(
        conversationId: String
    ): Conversation {
        require(
            conversationId.isNotBlank()
        ) {
            "Conversation ID cannot be blank."
        }

        /*
         * The learner profile may exist independently from the
         * conversation itself.
         */
        progressRepository.ensureProfileExists()

        /*
         * Existing conversation:
         *
         * restore normally.
         */
        val existingConversation =
            chatRepository.getConversation(
                conversationId =
                    conversationId
            )

        if (existingConversation != null) {
            return existingConversation
        }

        /*
         * Missing conversation:
         *
         * Return an in-memory provisional conversation.
         *
         * Nothing is written to Room here.
         */
        val currentInstant =
            clock.instant()

        val localDate =
            currentInstant
                .atZone(
                    clock.zone
                )
                .toLocalDate()

        val timestamp =
            currentInstant.toEpochMilli()

        return Conversation(
            id =
                conversationId,
            title =
                PROVISIONAL_CONVERSATION_TITLE,
            cefrLevel =
                com.julesmeister.deutschcraft.core.cefr.model.CefrLevel.A1,
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
                false,
            titleSource =
                com.julesmeister.deutschcraft.core.database.entity.ConversationTitleSource.DEFAULT
        )
    }

    private companion object {

        const val PROVISIONAL_CONVERSATION_TITLE =
            "Neue Unterhaltung"
    }
}