package com.julesmeister.deutschcraft.feature.archivedthreads.data

import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import com.julesmeister.deutschcraft.feature.archivedthreads.domain.ArchivedThread
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ArchivedThreadsRepository @Inject constructor(
    private val conversationDao: ConversationDao,
    private val clock: Clock
) {

    fun observeArchivedThreads(): Flow<List<ArchivedThread>> {
        return conversationDao
            .observeArchivedConversations()
            .map { conversations ->
                conversations.map { conversation ->
                    conversation.toArchivedThread()
                }
            }
    }

    suspend fun restoreThread(
        conversationId: String
    ) {
        conversationDao.restoreConversation(
            conversationId = conversationId,
            updatedAt = clock.millis(),
            syncStatus = SyncStatusEntity.LOCAL_ONLY
        )
    }

    suspend fun deleteThreadPermanently(
        conversationId: String
    ) {
        conversationDao.deleteConversationPermanently(
            conversationId = conversationId
        )
    }

    private fun ConversationEntity.toArchivedThread(): ArchivedThread {
        return ArchivedThread(
            id = id,
            title = title,
            previewText = previewText,
            cefrLevel = cefrLevel,
            messageCount = messageCount,
            updatedAt = updatedAt
        )
    }
}