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
     *
     * Mission analytics can use this to isolate messages that
     * occurred while one ConversationMission was active.
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
     *
     * Mission analytics uses this with:
     *
     * MessageSenderEntity.USER
     *
     * so assistant messages do not inflate learner-message
     * counts or Besser correction statistics.
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
     * Returns the largest sequence number currently persisted
     * for a conversation.
     *
     * A conversation with no messages returns 0.
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
     * Deletes one message.
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
     * Updates the Besser correction stored directly on a
     * learner message.
     *
     * null means that no meaningful correction is required.
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
     * Results are intentionally returned newest-first because
     * the SQL orders by sequenceNumber DESC.
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