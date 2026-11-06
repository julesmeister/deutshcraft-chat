// app/src/main/java/com/julesmeister/deutschcraft/core/database/dao/ConversationDao.kt

package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity
import com.julesmeister.deutschcraft.core.database.entity.ConversationTitleSource
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Upsert
    suspend fun upsertConversation(
        conversation:
        ConversationEntity
    )

    @Upsert
    suspend fun upsertConversations(
        conversations:
        List<ConversationEntity>
    )

    @Query(
        """
        SELECT *
        FROM conversations
        WHERE isArchived = 0
          AND isDeleted = 0
        ORDER BY updatedAt DESC
        """
    )
    fun observeActiveConversations():
            Flow<List<ConversationEntity>>

    @Query(
        """
        SELECT *
        FROM conversations
        WHERE isArchived = 0
          AND isDeleted = 0
          AND epochDay = :epochDay
        ORDER BY updatedAt DESC
        """
    )
    fun observeConversationsForDay(
        epochDay: Long
    ): Flow<List<ConversationEntity>>

    @Query(
        """
        SELECT *
        FROM conversations
        WHERE id = :conversationId
          AND isDeleted = 0
        LIMIT 1
        """
    )
    fun observeConversation(
        conversationId: String
    ): Flow<ConversationEntity?>

    @Query(
        """
        SELECT *
        FROM conversations
        WHERE id = :conversationId
        LIMIT 1
        """
    )
    suspend fun getConversationIncludingDeleted(
        conversationId: String
    ): ConversationEntity?

    @Query(
        """
        SELECT *
        FROM conversations
        WHERE id = :conversationId
          AND isDeleted = 0
        LIMIT 1
        """
    )
    suspend fun getConversation(
        conversationId: String
    ): ConversationEntity?

    @Query(
        """
        UPDATE conversations
        SET
            title = :title,
            titleSource = :titleSource,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        WHERE id = :conversationId
          AND titleSource = 'DEFAULT'
          AND isDeleted = 0
        """
    )
    suspend fun updateGeneratedTitle(
        conversationId: String,
        title: String,
        titleSource:
        ConversationTitleSource,
        updatedAt: Long,
        syncStatus:
        SyncStatusEntity
    ): Int

    @Query(
        """
        SELECT *
        FROM conversations
        WHERE isArchived = 0
          AND isClosed = 0
          AND isDeleted = 0
        ORDER BY updatedAt DESC
        LIMIT 1
        """
    )
    suspend fun getLatestOpenConversation():
            ConversationEntity?

    @Query(
        """
        SELECT *
        FROM conversations
        WHERE isArchived = 0
          AND isClosed = 0
          AND isDeleted = 0
          AND epochDay = :epochDay
        ORDER BY updatedAt DESC
        LIMIT 1
        """
    )
    suspend fun getOpenConversationForDay(
        epochDay: Long
    ): ConversationEntity?

    @Query(
        """
        UPDATE conversations
        SET
            updatedAt = :updatedAt,
            previewText = :previewText,
            messageCount = messageCount + 1,
            syncStatus = :syncStatus
        WHERE id = :conversationId
          AND isDeleted = 0
        """
    )
    suspend fun updateConversationAfterMessage(
        conversationId: String,
        previewText: String,
        updatedAt: Long,
        syncStatus:
        SyncStatusEntity
    ): Int

    @Query(
        """
        UPDATE conversations
        SET
            title = :title,
            titleSource = :titleSource,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        WHERE id = :conversationId
          AND isDeleted = 0
        """
    )
    suspend fun updateConversationTitle(
        conversationId: String,
        title: String,
        titleSource:
        ConversationTitleSource,
        updatedAt: Long,
        syncStatus:
        SyncStatusEntity
    ): Int

    @Query(
        """
        UPDATE conversations
        SET
            isClosed = 1,
            closedAt = :closedAt,
            updatedAt = :closedAt,
            syncStatus = :syncStatus
        WHERE id = :conversationId
          AND isDeleted = 0
        """
    )
    suspend fun closeConversation(
        conversationId: String,
        closedAt: Long,
        syncStatus:
        SyncStatusEntity
    ): Int

    @Query(
        """
        UPDATE conversations
        SET
            isArchived = 1,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        WHERE id = :conversationId
          AND isDeleted = 0
        """
    )
    suspend fun archiveConversation(
        conversationId: String,
        updatedAt: Long,
        syncStatus:
        SyncStatusEntity
    ): Int

    @Query(
        """
        SELECT *
        FROM conversations
        WHERE isArchived = 1
          AND isDeleted = 0
        ORDER BY updatedAt DESC
        """
    )
    fun observeArchivedConversations():
            Flow<List<ConversationEntity>>

    @Query(
        """
        UPDATE conversations
        SET
            isArchived = 0,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        WHERE id = :conversationId
          AND isDeleted = 0
        """
    )
    suspend fun restoreConversation(
        conversationId: String,
        updatedAt: Long,
        syncStatus:
        SyncStatusEntity
    ): Int

    @Query(
        """
        SELECT *
        FROM conversations
        WHERE syncStatus IN (
            'LOCAL_ONLY',
            'PENDING_UPLOAD',
            'PENDING_DELETE'
        )
        ORDER BY updatedAt ASC
        """
    )
    suspend fun getPendingSyncConversations():
            List<ConversationEntity>

    @Query(
        """
        SELECT COUNT(*)
        FROM conversations
        WHERE syncStatus IN (
            'LOCAL_ONLY',
            'PENDING_UPLOAD',
            'PENDING_DELETE'
        )
        """
    )
    suspend fun countPendingSyncConversations():
            Int

    @Query(
        """
        UPDATE conversations
        SET
            isDeleted = 1,
            deletedAt = :deletedAt,
            isArchived = 1,
            isClosed = 1,
            closedAt = COALESCE(
                closedAt,
                :deletedAt
            ),
            updatedAt = :deletedAt,
            syncStatus = 'PENDING_DELETE'
        WHERE id = :conversationId
          AND isDeleted = 0
        """
    )
    suspend fun markConversationDeleted(
        conversationId: String,
        deletedAt: Long
    ): Int

    @Query(
        """
        UPDATE conversations
        SET
            syncStatus = 'SYNCED',
            remoteVersion = :remoteVersion,
            lastSyncedAt = :lastSyncedAt
        WHERE id = :conversationId
          AND updatedAt = :expectedUpdatedAt
          AND syncStatus IN (
              'LOCAL_ONLY',
              'PENDING_UPLOAD',
              'PENDING_DELETE'
          )
        """
    )
    suspend fun markConversationSynced(
        conversationId: String,
        expectedUpdatedAt: Long,
        remoteVersion: Long,
        lastSyncedAt: Long
    ): Int

    @Query(
        """
        UPDATE conversations
        SET
            syncStatus = 'PENDING_UPLOAD',
            updatedAt = :updatedAt
        WHERE id = :conversationId
          AND isDeleted = 0
        """
    )
    suspend fun markConversationPendingUpload(
        conversationId: String,
        updatedAt: Long
    ): Int

    @Query(
        """
        DELETE FROM conversations
        WHERE id = :conversationId
        """
    )
    suspend fun deleteConversationPermanently(
        conversationId: String
    ): Int

    @Query(
        """
        DELETE FROM conversations
        WHERE isDeleted = 1
          AND syncStatus = 'SYNCED'
          AND lastSyncedAt IS NOT NULL
          AND lastSyncedAt <= :syncedBefore
        """
    )
    suspend fun purgeSyncedConversationTombstones(
        syncedBefore: Long
    ): Int

    @Query(
        """
        DELETE FROM conversations
        """
    )
    suspend fun deleteAllConversations()
}