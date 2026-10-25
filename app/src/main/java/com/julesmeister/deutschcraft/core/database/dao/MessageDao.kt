package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.julesmeister.deutschcraft.core.database.entity.MessageEntity
import com.julesmeister.deutschcraft.core.database.entity.MessageSenderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Upsert
    suspend fun upsertMessage(
        message: MessageEntity
    )

    @Upsert
    suspend fun upsertMessages(
        messages: List<MessageEntity>
    )

    @Query(
        """
        SELECT *
        FROM messages
        WHERE conversationId = :conversationId
        ORDER BY sequenceNumber ASC
        """
    )
    fun observeMessages(
        conversationId: String
    ): Flow<List<MessageEntity>>

    @Query(
        """
    SELECT *
    FROM messages
    WHERE conversationId = :conversationId
    ORDER BY sequenceNumber ASC
    """
    )
    suspend fun getMessages(
        conversationId: String
    ): List<MessageEntity>

    @Query(
        """
        SELECT *
        FROM messages
        WHERE id = :messageId
        LIMIT 1
        """
    )
    suspend fun getMessage(
        messageId: String
    ): MessageEntity?

    @Query(
        """
        SELECT COALESCE(MAX(sequenceNumber), 0)
        FROM messages
        WHERE conversationId = :conversationId
        """
    )
    suspend fun getLastSequenceNumber(
        conversationId: String
    ): Long

    @Query(
        """
        SELECT *
        FROM messages
        WHERE syncStatus IN (
            'PENDING_UPLOAD',
            'PENDING_DELETE'
        )
        ORDER BY createdAt ASC
        """
    )
    suspend fun getPendingSyncMessages():
            List<MessageEntity>

    @Query(
        """
        DELETE FROM messages
        WHERE id = :messageId
        """
    )
    suspend fun deleteMessage(
        messageId: String
    )

    @Query(
        """
    UPDATE messages
    SET correction = :correction
    WHERE id = :messageId
    """
    )
    suspend fun updateMessageCorrection(
        messageId: String,
        correction: String?
    ): Int

    @Query(
        """
        DELETE FROM messages
        WHERE conversationId = :conversationId
        """
    )
    suspend fun deleteMessagesForConversation(
        conversationId: String
    )

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}