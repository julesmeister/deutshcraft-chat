package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.julesmeister.deutschcraft.core.database.entity.besser.BesserChallengeEntity
import com.julesmeister.deutschcraft.core.database.entity.besser.BesserChallengeHintEntity
import com.julesmeister.deutschcraft.core.database.model.BesserChallengeWithHints
import kotlinx.coroutines.flow.Flow

@Dao
abstract class BesserChallengeDao {

    @Upsert
    protected abstract suspend fun upsertChallengeInternal(
        challenge: BesserChallengeEntity
    )

    @Upsert
    protected abstract suspend fun upsertHintsInternal(
        hints: List<BesserChallengeHintEntity>
    )

    @Query(
        """
        DELETE FROM besser_challenge_hints
        WHERE challenge_id = :challengeId
        """
    )
    protected abstract suspend fun deleteHintsInternal(
        challengeId: String
    )

    @Transaction
    open suspend fun upsertChallengeWithHints(
        challenge: BesserChallengeEntity,
        hints: List<BesserChallengeHintEntity>
    ) {
        require(
            hints.all { hint ->
                hint.challengeId == challenge.id
            }
        ) {
            "Every hint must belong to challenge ${challenge.id}."
        }

        require(
            hints.map { hint ->
                hint.position
            }.distinct().size == hints.size
        ) {
            "Hint positions must be unique inside one challenge."
        }

        upsertChallengeInternal(
            challenge = challenge
        )

        deleteHintsInternal(
            challengeId = challenge.id
        )

        if (hints.isNotEmpty()) {
            upsertHintsInternal(
                hints = hints
            )
        }
    }

    @Transaction
    @Query(
        """
        SELECT *
        FROM besser_challenges
        WHERE id = :challengeId
        LIMIT 1
        """
    )
    abstract suspend fun getById(
        challengeId: String
    ): BesserChallengeWithHints?

    @Transaction
    @Query(
        """
        SELECT *
        FROM besser_challenges
        WHERE id = :challengeId
        LIMIT 1
        """
    )
    abstract fun observeById(
        challengeId: String
    ): Flow<BesserChallengeWithHints?>

    @Transaction
    @Query(
        """
        SELECT *
        FROM besser_challenges
        WHERE message_id = :messageId
        ORDER BY created_at DESC
        """
    )
    abstract fun observeForMessage(
        messageId: String
    ): Flow<List<BesserChallengeWithHints>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM besser_challenges
        WHERE message_id = :messageId
            AND status IN (
                'AVAILABLE',
                'ACCEPTED'
            )
        ORDER BY created_at DESC
        LIMIT 1
        """
    )
    abstract fun observeActiveForMessage(
        messageId: String
    ): Flow<BesserChallengeWithHints?>

    @Transaction
    @Query(
        """
        SELECT *
        FROM besser_challenges
        WHERE conversation_id = :conversationId
        ORDER BY created_at ASC
        """
    )
    abstract fun observeForConversation(
        conversationId: String
    ): Flow<List<BesserChallengeWithHints>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM besser_challenges
        WHERE conversation_id = :conversationId
            AND status IN (
                'AVAILABLE',
                'ACCEPTED'
            )
        ORDER BY created_at ASC
        """
    )
    abstract fun observeActiveForConversation(
        conversationId: String
    ): Flow<List<BesserChallengeWithHints>>

    @Query(
        """
        SELECT *
        FROM besser_challenges
        WHERE message_id IN (:messageIds)
            AND status IN (
                'AVAILABLE',
                'ACCEPTED'
            )
        ORDER BY created_at ASC
        """
    )
    abstract suspend fun getActiveForMessages(
        messageIds: List<String>
    ): List<BesserChallengeEntity>

    @Query(
        """
        SELECT *
        FROM besser_challenges
        WHERE capability_id = :capabilityId
        ORDER BY created_at DESC
        """
    )
    abstract fun observeForCapability(
        capabilityId: String
    ): Flow<List<BesserChallengeEntity>>

    @Query(
        """
        UPDATE besser_challenges
        SET
            status = 'ACCEPTED',
            accepted_at = :acceptedAt,
            updated_at = :acceptedAt
        WHERE id = :challengeId
            AND status = 'AVAILABLE'
        """
    )
    abstract suspend fun accept(
        challengeId: String,
        acceptedAt: Long
    ): Int

    @Query(
        """
        UPDATE besser_challenges
        SET
            status = 'SKIPPED',
            skipped_at = :skippedAt,
            updated_at = :skippedAt
        WHERE id = :challengeId
            AND status IN (
                'AVAILABLE',
                'ACCEPTED'
            )
        """
    )
    abstract suspend fun skip(
        challengeId: String,
        skippedAt: Long
    ): Int

    @Query(
        """
        UPDATE besser_challenges
        SET
            status = 'COMPLETED',
            refined_message_content = :refinedMessageContent,
            actual_assistance_level = :actualAssistanceLevel,
            completed_at = :completedAt,
            updated_at = :completedAt
        WHERE id = :challengeId
            AND status IN (
                'AVAILABLE',
                'ACCEPTED'
            )
        """
    )
    abstract suspend fun complete(
        challengeId: String,
        refinedMessageContent: String,
        actualAssistanceLevel: String,
        completedAt: Long
    ): Int

    @Query(
        """
        UPDATE besser_challenges
        SET
            revealed_hint_count =
                CASE
                    WHEN revealed_hint_count < (
                        SELECT COUNT(*)
                        FROM besser_challenge_hints
                        WHERE challenge_id = :challengeId
                    )
                    THEN revealed_hint_count + 1
                    ELSE revealed_hint_count
                END,
            updated_at = :updatedAt
        WHERE id = :challengeId
            AND status IN (
                'AVAILABLE',
                'ACCEPTED'
            )
        """
    )
    abstract suspend fun revealNextHint(
        challengeId: String,
        updatedAt: Long
    ): Int

    @Query(
        """
        SELECT revealed_hint_count
        FROM besser_challenges
        WHERE id = :challengeId
        LIMIT 1
        """
    )
    abstract suspend fun getRevealedHintCount(
        challengeId: String
    ): Int?

    @Query(
        """
        SELECT COUNT(*)
        FROM besser_challenges
        WHERE message_id = :messageId
            AND status IN (
                'AVAILABLE',
                'ACCEPTED'
            )
        """
    )
    abstract suspend fun countActiveForMessage(
        messageId: String
    ): Int

    @Query(
        """
        DELETE FROM besser_challenges
        WHERE conversation_id = :conversationId
        """
    )
    abstract suspend fun deleteForConversation(
        conversationId: String
    ): Int

    @Query(
        """
        DELETE FROM besser_challenges
        WHERE message_id = :messageId
        """
    )
    abstract suspend fun deleteForMessage(
        messageId: String
    ): Int
}