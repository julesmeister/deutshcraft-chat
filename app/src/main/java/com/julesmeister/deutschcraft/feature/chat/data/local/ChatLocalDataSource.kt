package com.julesmeister.deutschcraft.feature.chat.data.local

import androidx.room.withTransaction
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity
import com.julesmeister.deutschcraft.core.database.entity.MessageEntity
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
        return messageDao.observeMessages(conversationId)
    }

    suspend fun getConversation(
        conversationId: String
    ): ConversationEntity? {
        return conversationDao.getConversation(conversationId)
    }

    suspend fun createConversation(
        conversation: ConversationEntity
    ) {
        conversationDao.upsertConversation(conversation)
    }

    suspend fun saveMessage(
        message: MessageEntity
    ) {
        database.withTransaction {
            messageDao.upsertMessage(message)

            conversationDao.updateConversationPreview(
                conversationId = message.conversationId,
                lastMessagePreview = message.content,
                updatedAt = message.createdAt,
                syncStatus = SyncStatusEntity.LOCAL_ONLY
            )
        }
    }

    suspend fun getNextSequenceNumber(
        conversationId: String
    ): Long {
        return messageDao.getLastSequenceNumber(
            conversationId
        ) + 1L
    }

    suspend fun deleteConversation(
        conversationId: String
    ) {
        conversationDao.deleteConversationPermanently(
            conversationId
        )
    }
}