// app/src/main/java/com/julesmeister/deutschcraft/feature/archivedthreads/data/ArchivedThreadsRepository.kt

package com.julesmeister.deutschcraft.feature.archivedthreads.data

import androidx.room.withTransaction
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import com.julesmeister.deutschcraft.feature.archivedthreads.domain.ArchivedThread
import com.julesmeister.deutschcraft.feature.sync.scheduling.SyncScheduler
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ArchivedThreadsRepository
@Inject constructor(
    private val database:
    DeutschCraftDatabase,

    private val conversationDao:
    ConversationDao,

    private val messageDao:
    MessageDao,

    private val clock:
    Clock,

    private val syncScheduler:
    SyncScheduler
) {

    fun observeArchivedThreads():
            Flow<List<ArchivedThread>> {
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
        val updatedRows =
            conversationDao.restoreConversation(
                conversationId =
                    conversationId,
                updatedAt =
                    clock.millis(),
                syncStatus =
                    SyncStatusEntity.PENDING_UPLOAD
            )

        if (updatedRows > 0) {
            syncScheduler.requestImmediateSync()
        }
    }

    suspend fun deleteThreadPermanently(
        conversationId: String
    ) {
        val deletedAt =
            clock.millis()

        val conversationDeleted =
            database.withTransaction {
                messageDao
                    .markMessagesDeletedForConversation(
                        conversationId =
                            conversationId,
                        deletedAt =
                            deletedAt
                    )

                conversationDao
                    .markConversationDeleted(
                        conversationId =
                            conversationId,
                        deletedAt =
                            deletedAt
                    ) > 0
            }

        if (conversationDeleted) {
            syncScheduler.requestImmediateSync()
        }
    }

    private fun ConversationEntity.toArchivedThread():
            ArchivedThread {
        return ArchivedThread(
            id =
                id,
            title =
                title,
            previewText =
                previewText,
            cefrLevel =
                cefrLevel,
            messageCount =
                messageCount,
            updatedAt =
                updatedAt
        )
    }
}