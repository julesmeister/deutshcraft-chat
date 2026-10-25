package com.julesmeister.deutschcraft.feature.threads.domain

import kotlinx.coroutines.flow.Flow

interface ThreadRepository {

    /**
     * Observes all non-archived conversations.
     *
     * Room emits a new list whenever a conversation
     * is created, updated, archived, or deleted.
     */
    fun observeThreads():
            Flow<List<ConversationThread>>

    /**
     * Retrieves one conversation thread.
     */
    suspend fun getThread(
        conversationId: String
    ): ConversationThread?

    /**
     * Archives a conversation without permanently
     * deleting its messages.
     */
    suspend fun archiveThread(
        conversationId: String
    )

    /**
     * Permanently deletes the conversation.
     *
     * Related messages are deleted when the foreign
     * key uses ON DELETE CASCADE.
     */
    suspend fun deleteThread(
        conversationId: String
    )
}