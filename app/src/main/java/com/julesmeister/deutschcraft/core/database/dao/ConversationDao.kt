package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Upsert
    suspend fun upsertConversation(
        conversation: ConversationEntity
    )

    @Upsert
    suspend fun upsertConversations(
        conversations: List<ConversationEntity>
    )

    @Query(
        """
        SELECT *
        FROM conversations
        WHERE isArchived = 0
        ORDER BY updatedAt DESC
        """
    )
    fun observeActiveConversations(): Flow<List<ConversationEntity>>

    @Query(
        """
        SELECT *
        FROM conversations
        WHERE id = :conversationId
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
    suspend fun getConversation(
        conversationId: String
    ): ConversationEntity?

    @Query(
        """
        UPDATE conversations
        SET
            updatedAt = :updatedAt,
            lastMessagePreview = :lastMessagePreview,
            syncStatus = :syncStatus
        WHERE id = :conversationId
        """
    )
    suspend fun updateConversationPreview(
        conversationId: String,
        lastMessagePreview: String,
        updatedAt: Long,
        syncStatus: SyncStatusEntity
    )

    @Query(
        """
        UPDATE conversations
        SET
            isArchived = 1,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        WHERE id = :conversationId
        """
    )
    suspend fun archiveConversation(
        conversationId: String,
        updatedAt: Long,
        syncStatus: SyncStatusEntity
    )

    @Query(
        """
        SELECT *
        FROM conversations
        WHERE syncStatus IN (
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
        DELETE FROM conversations
        WHERE id = :conversationId
        """
    )
    suspend fun deleteConversationPermanently(
        conversationId: String
    )

    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
}