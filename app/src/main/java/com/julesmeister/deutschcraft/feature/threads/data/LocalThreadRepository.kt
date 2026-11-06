// app/src/main/java/com/julesmeister/deutschcraft/feature/threads/data/LocalThreadRepository.kt

package com.julesmeister.deutschcraft.feature.threads.data

import androidx.room.withTransaction
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import com.julesmeister.deutschcraft.feature.sync.scheduling.SyncScheduler
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import com.julesmeister.deutschcraft.feature.threads.domain.ThreadRepository
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class LocalThreadRepository
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
                conversationId =
                    conversationId
            )
            ?.toThread()
    }

    override suspend fun archiveThread(
        conversationId: String
    ) {
        require(
            conversationId.isNotBlank()
        ) {
            "Conversation ID cannot be blank."
        }

        val affectedRows =
            conversationDao.archiveConversation(
                conversationId =
                    conversationId,
                updatedAt =
                    currentTimestamp(),
                syncStatus =
                    SyncStatusEntity.PENDING_UPLOAD
            )

        check(
            affectedRows > 0
        ) {
            "Conversation $conversationId could not be archived."
        }

        syncScheduler.requestImmediateSync()
    }

    override suspend fun restoreThread(
        conversationId: String
    ) {
        require(
            conversationId.isNotBlank()
        ) {
            "Conversation ID cannot be blank."
        }

        val affectedRows =
            conversationDao.restoreConversation(
                conversationId =
                    conversationId,
                updatedAt =
                    currentTimestamp(),
                syncStatus =
                    SyncStatusEntity.PENDING_UPLOAD
            )

        check(
            affectedRows > 0
        ) {
            "Conversation $conversationId could not be restored."
        }

        syncScheduler.requestImmediateSync()
    }

    override suspend fun deleteThread(
        conversationId: String
    ) {
        require(
            conversationId.isNotBlank()
        ) {
            "Conversation ID cannot be blank."
        }

        val deletedAt =
            currentTimestamp()

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

        check(
            conversationDeleted
        ) {
            "Conversation $conversationId could not be deleted."
        }

        syncScheduler.requestImmediateSync()
    }

    private fun currentTimestamp():
            Long {
        return clock
            .instant()
            .toEpochMilli()
    }

    private fun ConversationEntity.toThread():
            ConversationThread {
        return ConversationThread(
            id =
                id,
            title =
                title,
            previewText =
                previewText,
            cefrLevel =
                cefrLevel,
            localDate =
                localDate,
            epochDay =
                epochDay,
            messageCount =
                messageCount,
            updatedAt =
                updatedAt,
            isClosed =
                isClosed
        )
    }
}