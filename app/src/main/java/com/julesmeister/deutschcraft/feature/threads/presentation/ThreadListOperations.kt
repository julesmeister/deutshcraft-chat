package com.julesmeister.deutschcraft.feature.threads.presentation

import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.threads.domain.ThreadRepository
import javax.inject.Inject

class ThreadListOperations @Inject constructor(
    private val threadRepository:
    ThreadRepository,
    private val chatRepository:
    ChatRepository
) {

    suspend fun createConversation():
            Conversation {
        return chatRepository.createConversation()
    }

    suspend fun archiveConversation(
        conversationId: String,
        conversationTitle: String
    ): ArchiveSnackbarEvent {
        threadRepository.archiveThread(
            conversationId =
                conversationId
        )

        return ArchiveSnackbarEvent(
            conversationId =
                conversationId,
            conversationTitle =
                conversationTitle
        )
    }

    suspend fun restoreConversation(
        conversationId: String
    ) {
        threadRepository.restoreThread(
            conversationId =
                conversationId
        )
    }

    suspend fun deleteConversation(
        conversationId: String
    ) {
        threadRepository.deleteThread(
            conversationId =
                conversationId
        )
    }
}