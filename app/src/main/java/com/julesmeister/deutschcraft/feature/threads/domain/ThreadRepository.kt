package com.julesmeister.deutschcraft.feature.threads.domain

import kotlinx.coroutines.flow.Flow

interface ThreadRepository {

    fun observeThreads():
            Flow<List<ConversationThread>>

    fun observeArchivedThreads():
            Flow<List<ConversationThread>>

    suspend fun getThread(
        conversationId: String
    ): ConversationThread?

    suspend fun archiveThread(
        conversationId: String
    )

    suspend fun restoreThread(
        conversationId: String
    )

    suspend fun deleteThread(
        conversationId: String
    )
}