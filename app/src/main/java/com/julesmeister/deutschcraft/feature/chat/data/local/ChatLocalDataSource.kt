package com.julesmeister.deutschcraft.feature.chat.data.local

import androidx.room.withTransaction
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity
import com.julesmeister.deutschcraft.core.database.entity.ConversationTitleSource
import com.julesmeister.deutschcraft.core.database.entity.MessageEntity
import com.julesmeister.deutschcraft.core.database.entity.MessageSenderEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ChatLocalDataSource @Inject constructor(
    private val database: DeutschCraftDatabase,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
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
            conversation = conversation
        )
    }

    suspend fun saveMessage(
        message: MessageEntity
    ) {
        database.withTransaction {
            messageDao.upsertMessage(
                message = message
            )

            conversationDao
                .updateConversationAfterMessage(
                    conversationId =
                        message.conversationId,
                    previewText =
                        message.content,
                    updatedAt =
                        message.createdAt,
                    syncStatus =
                        SyncStatusEntity.LOCAL_ONLY
                )
        }
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
        return messageDao.getLastSequenceNumber(
            conversationId = conversationId
        ) + 1L
    }

    suspend fun deleteConversation(
        conversationId: String
    ) {
        conversationDao
            .deleteConversationPermanently(
                conversationId = conversationId
            )
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
                    SyncStatusEntity.LOCAL_ONLY
            )

        return updatedRows > 0
    }

    suspend fun updateMessageCorrection(
        messageId: String,
        correction: String?
    ): Boolean {
        val updatedRows =
            messageDao.updateMessageCorrection(
                messageId = messageId,
                correction = correction,
            )

        return updatedRows > 0
    }
}