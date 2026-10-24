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

    /*
     * Create or replace one conversation.
     */
    @Upsert
    suspend fun upsertConversation(
        conversation: ConversationEntity
    )

    /*
     * Create or replace multiple conversations.
     *
     * This will become useful when cloud synchronization
     * is added for subscribed users.
     */
    @Upsert
    suspend fun upsertConversations(
        conversations: List<ConversationEntity>
    )

    /*
     * Observe all visible conversation threads.
     *
     * Whenever a conversation is inserted, updated,
     * archived, or deleted, Room emits a new list.
     */
    @Query(
        """
        SELECT *
        FROM conversations
        WHERE isArchived = 0
        ORDER BY updatedAt DESC
        """
    )
    fun observeActiveConversations():
            Flow<List<ConversationEntity>>

    /*
     * Observe all conversations belonging to one day.
     *
     * This can support date-grouped thread sections.
     */
    @Query(
        """
        SELECT *
        FROM conversations
        WHERE isArchived = 0
          AND epochDay = :epochDay
        ORDER BY updatedAt DESC
        """
    )
    fun observeConversationsForDay(
        epochDay: Long
    ): Flow<List<ConversationEntity>>

    /*
     * Observe one conversation continuously.
     */
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

    /*
     * Retrieve one conversation once.
     */
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

    /*
     * Retrieve the newest open conversation.
     *
     * This will help determine whether the current
     * conversation can continue or whether a new daily
     * conversation should be created.
     */
    @Query(
        """
        SELECT *
        FROM conversations
        WHERE isArchived = 0
          AND isClosed = 0
        ORDER BY updatedAt DESC
        LIMIT 1
        """
    )
    suspend fun getLatestOpenConversation():
            ConversationEntity?

    /*
     * Retrieve the newest open conversation for a
     * particular local calendar day.
     */
    @Query(
        """
        SELECT *
        FROM conversations
        WHERE isArchived = 0
          AND isClosed = 0
          AND epochDay = :epochDay
        ORDER BY updatedAt DESC
        LIMIT 1
        """
    )
    suspend fun getOpenConversationForDay(
        epochDay: Long
    ): ConversationEntity?

    /*
     * Update the thread metadata after a message
     * has been inserted.
     *
     * The parameter name previewText must match
     * the SQL bind variable :previewText.
     */
    @Query(
        """
        UPDATE conversations
        SET
            updatedAt = :updatedAt,
            previewText = :previewText,
            messageCount = messageCount + 1,
            syncStatus = :syncStatus
        WHERE id = :conversationId
        """
    )
    suspend fun updateConversationAfterMessage(
        conversationId: String,
        previewText: String,
        updatedAt: Long,
        syncStatus: SyncStatusEntity
    ): Int

    /*
     * Apply a locally generated or AI-generated title.
     */
    @Query(
        """
        UPDATE conversations
        SET
            title = :title,
            titleSource = :titleSource,
            updatedAt = :updatedAt,
            syncStatus = :syncStatus
        WHERE id = :conversationId
        """
    )
    suspend fun updateConversationTitle(
        conversationId: String,
        title: String,
        titleSource: ConversationTitleSource,
        updatedAt: Long,
        syncStatus: SyncStatusEntity
    ): Int

    /*
     * Close a daily conversation.
     *
     * Closed conversations remain visible in thread
     * history but should no longer receive messages.
     */
    @Query(
        """
        UPDATE conversations
        SET
            isClosed = 1,
            closedAt = :closedAt,
            updatedAt = :closedAt,
            syncStatus = :syncStatus
        WHERE id = :conversationId
        """
    )
    suspend fun closeConversation(
        conversationId: String,
        closedAt: Long,
        syncStatus: SyncStatusEntity
    ): Int

    /*
     * Archive a conversation without permanently
     * deleting its messages.
     */
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
    ): Int

    /*
     * Retrieve conversations awaiting future cloud
     * synchronization.
     */
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

    /*
     * Permanently delete one conversation.
     *
     * Related messages should also be deleted if the
     * MessageEntity foreign key uses CASCADE.
     */
    @Query(
        """
        DELETE FROM conversations
        WHERE id = :conversationId
        """
    )
    suspend fun deleteConversationPermanently(
        conversationId: String
    ): Int

    /*
     * Development or account-deletion operation.
     */
    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
}