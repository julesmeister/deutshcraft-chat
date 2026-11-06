// app/src/main/java/com/julesmeister/deutschcraft/core/database/dao/MessageDao.kt

package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.julesmeister.deutschcraft.core.database.entity.MessageEntity
import com.julesmeister.deutschcraft.core.database.entity.MessageSenderEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Upsert
    suspend fun upsertMessage(
        message:
        MessageEntity
    )

    @Upsert
    suspend fun upsertMessages(
        messages:
        List<MessageEntity>
    )

    @Query(
        """
        SELECT *
        FROM messages
        WHERE conversationId = :conversationId
          AND isDeleted = 0
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
          AND isDeleted = 0
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
        WHERE conversationId = :conversationId
          AND isDeleted = 0
          AND createdAt >= :startedAt
          AND createdAt <= :completedAt
        ORDER BY sequenceNumber ASC
        """
    )
    suspend fun getMessagesInWindow(
        conversationId: String,
        startedAt: Long,
        completedAt: Long
    ): List<MessageEntity>

    @Query(
        """
        SELECT *
        FROM messages
        WHERE conversationId = :conversationId
          AND sender = :sender
          AND isDeleted = 0
          AND createdAt >= :startedAt
          AND createdAt <= :completedAt
        ORDER BY sequenceNumber ASC
        """
    )
    suspend fun getMessagesForSenderInWindow(
        conversationId: String,
        sender:
        MessageSenderEntity,
        startedAt: Long,
        completedAt: Long
    ): List<MessageEntity>

    @Query(
        """
        SELECT *
        FROM messages
        WHERE id = :messageId
          AND isDeleted = 0
        LIMIT 1
        """
    )
    suspend fun getMessage(
        messageId: String
    ): MessageEntity?

    @Query(
        """
        SELECT *
        FROM messages
        WHERE id = :messageId
        LIMIT 1
        """
    )
    suspend fun getMessageIncludingDeleted(
        messageId: String
    ): MessageEntity?

    @Query(
        """
        SELECT COALESCE(
            MAX(sequenceNumber),
            0
        )
        FROM messages
        WHERE conversationId = :conversationId
          AND isDeleted = 0
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
            'LOCAL_ONLY',
            'PENDING_UPLOAD',
            'PENDING_DELETE'
        )
        ORDER BY updatedAt ASC
        """
    )
    suspend fun getPendingSyncMessages():
            List<MessageEntity>

    @Query(
        """
        SELECT COUNT(*)
        FROM messages
        WHERE syncStatus IN (
            'LOCAL_ONLY',
            'PENDING_UPLOAD',
            'PENDING_DELETE'
        )
        """
    )
    suspend fun countPendingSyncMessages():
            Int

    @Query(
        """
        UPDATE messages
        SET
            correction = :correction,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        WHERE id = :messageId
          AND isDeleted = 0
        """
    )
    suspend fun updateMessageCorrection(
        messageId: String,
        correction: String?,
        updatedAt: Long,
        syncStatus:
        SyncStatusEntity
    ): Int

    @Query(
        """
        UPDATE messages
        SET
            content = :refinedContent,
            correction = NULL,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        WHERE id = :messageId
          AND sender = 'USER'
          AND isDeleted = 0
        """
    )
    suspend fun replaceLearnerMessageContent(
        messageId: String,
        refinedContent: String,
        updatedAt: Long,
        syncStatus:
        SyncStatusEntity
    ): Int

    @Query(
        """
        UPDATE messages
        SET
            isDeleted = 1,
            deletedAt = :deletedAt,
            updatedAt = :deletedAt,
            syncStatus = 'PENDING_DELETE'
        WHERE id = :messageId
          AND isDeleted = 0
        """
    )
    suspend fun markMessageDeleted(
        messageId: String,
        deletedAt: Long
    ): Int

    @Query(
        """
        UPDATE messages
        SET
            isDeleted = 1,
            deletedAt = :deletedAt,
            updatedAt = :deletedAt,
            syncStatus = 'PENDING_DELETE'
        WHERE conversationId = :conversationId
          AND isDeleted = 0
        """
    )
    suspend fun markMessagesDeletedForConversation(
        conversationId: String,
        deletedAt: Long
    ): Int

    @Query(
        """
        UPDATE messages
        SET
            syncStatus = 'PENDING_UPLOAD',
            updatedAt = :updatedAt
        WHERE id = :messageId
          AND isDeleted = 0
        """
    )
    suspend fun markMessagePendingUpload(
        messageId: String,
        updatedAt: Long
    ): Int

    @Query(
        """
        UPDATE messages
        SET
            syncStatus = 'SYNCED',
            remoteVersion = :remoteVersion,
            lastSyncedAt = :lastSyncedAt
        WHERE id = :messageId
          AND updatedAt = :expectedUpdatedAt
          AND syncStatus IN (
              'LOCAL_ONLY',
              'PENDING_UPLOAD',
              'PENDING_DELETE'
          )
        """
    )
    suspend fun markMessageSynced(
        messageId: String,
        expectedUpdatedAt: Long,
        remoteVersion: Long,
        lastSyncedAt: Long
    ): Int

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
        DELETE FROM messages
        WHERE conversationId = :conversationId
        """
    )
    suspend fun deleteMessagesForConversation(
        conversationId: String
    )

    @Query(
        """
        DELETE FROM messages
        WHERE isDeleted = 1
          AND syncStatus = 'SYNCED'
          AND lastSyncedAt IS NOT NULL
          AND lastSyncedAt <= :syncedBefore
        """
    )
    suspend fun purgeSyncedMessageTombstones(
        syncedBefore: Long
    ): Int

    @Query(
        """
        DELETE FROM messages
        """
    )
    suspend fun deleteAllMessages()

    @Query(
        """
        SELECT *
        FROM messages
        WHERE conversationId = :conversationId
          AND isDeleted = 0
        ORDER BY sequenceNumber DESC
        LIMIT :messageLimit
        """
    )
    fun observeRecentMessages(
        conversationId: String,
        messageLimit: Int
    ): Flow<List<MessageEntity>>
}