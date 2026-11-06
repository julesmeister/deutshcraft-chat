// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/data/local/ChatLocalDataSource.kt

package com.julesmeister.deutschcraft.feature.chat.data.local

import androidx.room.withTransaction
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity
import com.julesmeister.deutschcraft.core.database.entity.ConversationTitleSource
import com.julesmeister.deutschcraft.core.database.entity.MessageEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import com.julesmeister.deutschcraft.feature.sync.scheduling.SyncScheduler
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ChatLocalDataSource
@Inject constructor(
    private val database:
    DeutschCraftDatabase,

    private val conversationDao:
    ConversationDao,

    private val messageDao:
    MessageDao,

    private val syncScheduler:
    SyncScheduler
) {

    fun observeMessages(
        conversationId: String
    ): Flow<List<MessageEntity>> {
        return messageDao.observeMessages(
            conversationId = conversationId
        )
    }

    suspend fun getMessages(
        conversationId: String
    ): List<MessageEntity> {
        return messageDao.getMessages(
            conversationId = conversationId
        )
    }

    suspend fun getConversation(
        conversationId: String
    ): ConversationEntity? {
        return conversationDao.getConversation(
            conversationId = conversationId
        )
    }

    suspend fun createConversation(
        conversation: ConversationEntity
    ) {
        conversationDao.upsertConversation(
            conversation =
                conversation.copy(
                    syncStatus =
                        SyncStatusEntity.LOCAL_ONLY,
                    remoteVersion =
                        null,
                    lastSyncedAt =
                        null,
                    isDeleted =
                        false,
                    deletedAt =
                        null
                )
        )

        syncScheduler.requestImmediateSync()
    }

    suspend fun saveMessage(
        message: MessageEntity
    ) {
        val updatedAt =
            message.updatedAt.coerceAtLeast(
                message.createdAt
            )

        database.withTransaction {
            messageDao.upsertMessage(
                message =
                    message.copy(
                        updatedAt =
                            updatedAt,
                        syncStatus =
                            SyncStatusEntity.LOCAL_ONLY,
                        isDeleted =
                            false,
                        deletedAt =
                            null
                    )
            )

            conversationDao
                .updateConversationAfterMessage(
                    conversationId =
                        message.conversationId,
                    previewText =
                        message.content,
                    updatedAt =
                        updatedAt,
                    syncStatus =
                        SyncStatusEntity.PENDING_UPLOAD
                )
        }

        syncScheduler.requestImmediateSync()
    }

    suspend fun getOpenConversationForDay(
        epochDay: Long
    ): ConversationEntity? {
        return conversationDao
            .getOpenConversationForDay(
                epochDay = epochDay
            )
    }

    suspend fun getNextSequenceNumber(
        conversationId: String
    ): Long {
        return messageDao
            .getLastSequenceNumber(
                conversationId =
                    conversationId
            ) + 1L
    }

    suspend fun deleteConversation(
        conversationId: String,
        deletedAt: Long
    ): Boolean {
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

        return conversationDeleted
    }

    suspend fun updateGeneratedConversationTitle(
        conversationId: String,
        title: String,
        updatedAt: Long
    ): Boolean {
        val updatedRows =
            conversationDao.updateGeneratedTitle(
                conversationId =
                    conversationId,
                title =
                    title,
                titleSource =
                    ConversationTitleSource.LOCAL_GENERATED,
                updatedAt =
                    updatedAt,
                syncStatus =
                    SyncStatusEntity.PENDING_UPLOAD
            )

        val wasUpdated =
            updatedRows > 0

        if (wasUpdated) {
            syncScheduler.requestImmediateSync()
        }

        return wasUpdated
    }

    suspend fun updateMessageCorrection(
        messageId: String,
        correction: String?,
        updatedAt: Long
    ): Boolean {
        val updatedRows =
            messageDao.updateMessageCorrection(
                messageId =
                    messageId,
                correction =
                    correction,
                updatedAt =
                    updatedAt,
                syncStatus =
                    SyncStatusEntity.PENDING_UPLOAD
            )

        val wasUpdated =
            updatedRows > 0

        if (wasUpdated) {
            syncScheduler.requestImmediateSync()
        }

        return wasUpdated
    }
}