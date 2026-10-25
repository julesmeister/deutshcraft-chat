package com.julesmeister.deutschcraft.feature.threads.data

import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import com.julesmeister.deutschcraft.feature.threads.domain.ThreadRepository
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class LocalThreadRepository @Inject constructor(
    private val conversationDao: ConversationDao,
    private val clock: Clock
) : ThreadRepository {

    override fun observeThreads():
            Flow<List<ConversationThread>> {
        return conversationDao
            .observeActiveConversations()
            .map { conversations ->
                conversations.map { conversation ->
                    conversation.toThread()
                }
            }
    }

    override fun observeArchivedThreads():
            Flow<List<ConversationThread>> {
        return conversationDao
            .observeArchivedConversations()
            .map { conversations ->
                conversations.map { conversation ->
                    conversation.toThread()
                }
            }
    }

    override suspend fun getThread(
        conversationId: String
    ): ConversationThread? {
        return conversationDao
            .getConversation(
                conversationId = conversationId
            )
            ?.toThread()
    }

    override suspend fun archiveThread(
        conversationId: String
    ) {
        val affectedRows =
            conversationDao.archiveConversation(
                conversationId = conversationId,
                updatedAt = currentTimestamp(),
                syncStatus =
                    SyncStatusEntity.LOCAL_ONLY
            )

        check(affectedRows > 0) {
            "Conversation $conversationId could not be archived."
        }
    }

    override suspend fun restoreThread(
        conversationId: String
    ) {
        val affectedRows =
            conversationDao.restoreConversation(
                conversationId = conversationId,
                updatedAt = currentTimestamp(),
                syncStatus =
                    SyncStatusEntity.LOCAL_ONLY
            )

        check(affectedRows > 0) {
            "Conversation $conversationId could not be restored."
        }
    }

    override suspend fun deleteThread(
        conversationId: String
    ) {
        val affectedRows =
            conversationDao
                .deleteConversationPermanently(
                    conversationId = conversationId
                )

        check(affectedRows > 0) {
            "Conversation $conversationId could not be deleted."
        }
    }

    private fun currentTimestamp(): Long {
        return clock.instant().toEpochMilli()
    }

    private fun ConversationEntity.toThread():
            ConversationThread {
        return ConversationThread(
            id = id,
            title = title,
            previewText = previewText,
            cefrLevel = cefrLevel,
            localDate = localDate,
            epochDay = epochDay,
            messageCount = messageCount,
            updatedAt = updatedAt,
            isClosed = isClosed
        )
    }
}