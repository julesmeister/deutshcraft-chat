package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.julesmeister.deutschcraft.core.database.entity.learning.MarkedPhraseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkedPhraseDao {

    /*
     * Returns -1 when the same phrase was already marked on
     * the same message.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(phrase: MarkedPhraseEntity): Long

    @Query(
        """
        SELECT *
        FROM marked_phrases
        WHERE conversationId = :conversationId
        ORDER BY createdAt ASC
        """
    )
    fun observeForConversation(
        conversationId: String
    ): Flow<List<MarkedPhraseEntity>>

    @Query(
        """
        UPDATE marked_phrases
        SET meaning = :meaning,
            explanation = :explanation,
            example = :example,
            explanationStatus = :status,
            updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updateExplanation(
        id: String,
        meaning: String?,
        explanation: String?,
        example: String?,
        status: String,
        updatedAt: Long
    ): Int

    @Query(
        """
        UPDATE marked_phrases
        SET explanationStatus = :status,
            updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updateStatus(
        id: String,
        status: String,
        updatedAt: Long
    ): Int

    @Query("DELETE FROM marked_phrases WHERE id = :id")
    suspend fun delete(id: String): Int

    /*
     * Most recent marks across all conversations.
     * Used later to bring weak points back into practice.
     */
    @Query(
        """
        SELECT *
        FROM marked_phrases
        ORDER BY createdAt DESC
        LIMIT :limit
        """
    )
    suspend fun getRecent(limit: Int): List<MarkedPhraseEntity>
}