package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.julesmeister.deutschcraft.core.database.entity.adaptivelearning.AdaptiveLearningEvidenceEntity
import com.julesmeister.deutschcraft.core.database.entity.adaptivelearning.AdaptiveLearningFocusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdaptiveLearningDao {

    @Upsert
    suspend fun upsertFocus(
        focus: AdaptiveLearningFocusEntity
    )

    @Upsert
    suspend fun upsertFocuses(
        focuses: List<AdaptiveLearningFocusEntity>
    )

    @Upsert
    suspend fun upsertEvidence(
        evidence: AdaptiveLearningEvidenceEntity
    )

    @Upsert
    suspend fun upsertEvidenceList(
        evidence: List<AdaptiveLearningEvidenceEntity>
    )

    /*
     * Observes every known adaptive-learning focus.
     *
     * Focuses requiring attention appear first, followed by
     * improving and mastered focuses.
     */
    @Query(
        """
        SELECT *
        FROM adaptive_learning_focuses
        ORDER BY
            CASE masteryState
                WHEN 'STRUGGLING' THEN 0
                WHEN 'NEEDS_REVIEW' THEN 1
                WHEN 'PRACTICING' THEN 2
                WHEN 'IMPROVING' THEN 3
                WHEN 'OBSERVING' THEN 4
                WHEN 'MASTERED' THEN 5
                ELSE 6
            END ASC,
            confidence DESC,
            lastObservedAt DESC
        """
    )
    fun observeAllFocuses():
            Flow<List<AdaptiveLearningFocusEntity>>

    /*
     * Observes focuses that Emil should reinforce through
     * future conversations.
     */
    @Query(
        """
        SELECT *
        FROM adaptive_learning_focuses
        WHERE masteryState IN (
            'STRUGGLING',
            'PRACTICING',
            'NEEDS_REVIEW'
        )
        ORDER BY
            confidence DESC,
            struggleCount DESC,
            lastObservedAt DESC
        """
    )
    fun observeReinforcementFocuses():
            Flow<List<AdaptiveLearningFocusEntity>>

    /*
     * Observes skills the learner has demonstrated
     * consistently enough to mark as mastered.
     */
    @Query(
        """
        SELECT *
        FROM adaptive_learning_focuses
        WHERE masteryState = 'MASTERED'
        ORDER BY
            masteredAt DESC,
            updatedAt DESC
        """
    )
    fun observeMasteredFocuses():
            Flow<List<AdaptiveLearningFocusEntity>>

    @Query(
        """
        SELECT *
        FROM adaptive_learning_focuses
        WHERE type = :type
        ORDER BY
            CASE masteryState
                WHEN 'STRUGGLING' THEN 0
                WHEN 'NEEDS_REVIEW' THEN 1
                WHEN 'PRACTICING' THEN 2
                WHEN 'IMPROVING' THEN 3
                WHEN 'OBSERVING' THEN 4
                WHEN 'MASTERED' THEN 5
                ELSE 6
            END ASC,
            confidence DESC,
            lastObservedAt DESC
        """
    )
    fun observeFocusesByType(
        type: String
    ): Flow<List<AdaptiveLearningFocusEntity>>

    @Query(
        """
        SELECT *
        FROM adaptive_learning_focuses
        WHERE id = :focusId
        LIMIT 1
        """
    )
    suspend fun getFocusById(
        focusId: String
    ): AdaptiveLearningFocusEntity?

    @Query(
        """
        SELECT *
        FROM adaptive_learning_focuses
        WHERE focusKey = :focusKey
        LIMIT 1
        """
    )
    suspend fun getFocusByKey(
        focusKey: String
    ): AdaptiveLearningFocusEntity?

    /*
     * Returns the highest-priority learning weaknesses for
     * use by chat generation, missions, and recommendations.
     */
    @Query(
        """
        SELECT *
        FROM adaptive_learning_focuses
        WHERE masteryState IN (
            'STRUGGLING',
            'PRACTICING',
            'NEEDS_REVIEW'
        )
          AND confidence >= :minimumConfidence
        ORDER BY
            CASE masteryState
                WHEN 'STRUGGLING' THEN 0
                WHEN 'NEEDS_REVIEW' THEN 1
                WHEN 'PRACTICING' THEN 2
                ELSE 3
            END ASC,
            confidence DESC,
            struggleCount DESC,
            lastObservedAt DESC
        LIMIT :limit
        """
    )
    suspend fun getTopReinforcementFocuses(
        minimumConfidence: Float,
        limit: Int
    ): List<AdaptiveLearningFocusEntity>

    @Query(
        """
        SELECT *
        FROM adaptive_learning_evidence
        WHERE focusId = :focusId
        ORDER BY observedAt DESC
        """
    )
    fun observeEvidenceForFocus(
        focusId: String
    ): Flow<List<AdaptiveLearningEvidenceEntity>>

    @Query(
        """
        SELECT *
        FROM adaptive_learning_evidence
        WHERE focusId = :focusId
        ORDER BY observedAt DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentEvidence(
        focusId: String,
        limit: Int
    ): List<AdaptiveLearningEvidenceEntity>

    /*
     * Prevents the same learner message from contributing
     * evidence more than once for the same learning focus.
     */
    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM adaptive_learning_evidence
            WHERE focusId = :focusId
              AND messageId = :messageId
        )
        """
    )
    suspend fun evidenceExists(
        focusId: String,
        messageId: String
    ): Boolean

    /*
     * Saves the current aggregate focus state and the
     * individual message evidence as one Room transaction.
     */
    @Transaction
    suspend fun saveFocusWithEvidence(
        focus: AdaptiveLearningFocusEntity,
        evidence: AdaptiveLearningEvidenceEntity
    ) {
        upsertFocus(
            focus =
                focus
        )

        upsertEvidence(
            evidence =
                evidence
        )
    }

    @Query(
        """
        DELETE FROM adaptive_learning_evidence
        WHERE focusId = :focusId
        """
    )
    suspend fun deleteEvidenceForFocus(
        focusId: String
    )

    @Query(
        """
        DELETE FROM adaptive_learning_focuses
        WHERE id = :focusId
        """
    )
    suspend fun deleteFocus(
        focusId: String
    )

    @Transaction
    suspend fun deleteFocusWithEvidence(
        focusId: String
    ) {
        deleteEvidenceForFocus(
            focusId =
                focusId
        )

        deleteFocus(
            focusId =
                focusId
        )
    }

    @Query(
        """
        DELETE FROM adaptive_learning_evidence
        """
    )
    suspend fun deleteAllEvidence()

    @Query(
        """
        DELETE FROM adaptive_learning_focuses
        """
    )
    suspend fun deleteAllFocuses()

    @Transaction
    suspend fun clearAdaptiveLearningData() {
        deleteAllEvidence()
        deleteAllFocuses()
    }
}