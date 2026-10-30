package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.julesmeister.deutschcraft.core.database.entity.MessageEntity
import com.julesmeister.deutschcraft.core.database.entity.MessageSenderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    /*
     * Inserts or updates one message.
     */
    @Upsert
    suspend fun upsertMessage(
        message:
        MessageEntity
    )

    /*
     * Inserts or updates multiple messages.
     */
    @Upsert
    suspend fun upsertMessages(
        messages:
        List<MessageEntity>
    )

    /*
     * Observes the complete conversation in chronological
     * order.
     */
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

    /*
     * Loads the complete conversation once.
     */
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

    /*
     * Returns every message created during a specific time
     * window.
     */
    @Query(
        """
        SELECT *
        FROM messages
        WHERE conversationId = :conversationId
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

    /*
     * Returns messages from one sender during a given time
     * window.
     */
    @Query(
        """
        SELECT *
        FROM messages
        WHERE conversationId = :conversationId
          AND sender = :sender
          AND createdAt >= :startedAt
          AND createdAt <= :completedAt
        ORDER BY sequenceNumber ASC
        """
    )
    suspend fun getMessagesForSenderInWindow(
        conversationId: String,
        sender: MessageSenderEntity,
        startedAt: Long,
        completedAt: Long
    ): List<MessageEntity>

    /*
     * Loads one message by its primary key.
     */
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

    /*
     * Returns the largest persisted sequence number for one
     * conversation.
     */
    @Query(
        """
        SELECT COALESCE(
            MAX(sequenceNumber),
            0
        )
        FROM messages
        WHERE conversationId = :conversationId
        """
    )
    suspend fun getLastSequenceNumber(
        conversationId: String
    ): Long

    /*
     * Returns local messages waiting for synchronization.
     */
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

    /*
     * Permanently deletes one message.
     */
    @Query(
        """
        DELETE FROM messages
        WHERE id = :messageId
        """
    )
    suspend fun deleteMessage(
        messageId: String
    )

    /*
     * Updates the automatic correction attached to a learner
     * message.
     *
     * null means that no meaningful correction is currently
     * attached.
     */
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

    /*
     * Replaces the source learner message with the sentence
     * written during a completed Besser refinement.
     *
     * The old automatic correction is cleared because it was
     * generated for the previous message content and is now
     * stale.
     *
     * The sender condition protects against accidentally
     * rewriting an assistant message if malformed challenge
     * data ever reaches this operation.
     */
    @Query(
        """
        UPDATE messages
        SET
            content = :refinedContent,
            correction = NULL
        WHERE id = :messageId
          AND sender = 'USER'
        """
    )
    suspend fun replaceLearnerMessageContent(
        messageId: String,
        refinedContent: String
    ): Int

    /*
     * Deletes every message belonging to one conversation.
     */
    @Query(
        """
        DELETE FROM messages
        WHERE conversationId = :conversationId
        """
    )
    suspend fun deleteMessagesForConversation(
        conversationId: String
    )

    /*
     * Deletes every message in the database.
     */
    @Query(
        """
        DELETE FROM messages
        """
    )
    suspend fun deleteAllMessages()

    /*
     * Observes the latest messages for one conversation.
     *
     * Results are newest-first.
     */
    @Query(
        """
        SELECT *
        FROM messages
        WHERE conversationId = :conversationId
        ORDER BY sequenceNumber DESC
        LIMIT :messageLimit
        """
    )
    fun observeRecentMessages(
        conversationId: String,
        messageLimit: Int
    ): Flow<List<MessageEntity>>
}