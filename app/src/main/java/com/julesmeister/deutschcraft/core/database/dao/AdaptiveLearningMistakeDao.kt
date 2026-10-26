package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.julesmeister.deutschcraft.core.database.entity.DetectedMistakeEntity

/*
 * Read-only DAO used by adaptive learning to retrieve
 * mistakes already saved by the assessment system.
 *
 * Assessment remains responsible for creating and saving
 * DetectedMistakeEntity records.
 *
 * Adaptive learning only consumes those records.
 */
@Dao
interface AdaptiveLearningMistakeDao {

    /*
     * Returns every detected mistake belonging to one saved
     * assessment-evidence record.
     */
    @Query(
        """
        SELECT *
        FROM detected_mistakes
        WHERE evidenceId = :evidenceId
        ORDER BY
            isResolved ASC,
            occurrenceCount DESC,
            lastObservedAt DESC
        """
    )
    suspend fun getMistakesForEvidence(
        evidenceId: String
    ): List<DetectedMistakeEntity>

    /*
     * Returns unresolved mistakes for one assessment.
     *
     * This is useful when only current weaknesses should be
     * converted into adaptive-learning observations.
     */
    @Query(
        """
        SELECT *
        FROM detected_mistakes
        WHERE evidenceId = :evidenceId
          AND isResolved = 0
        ORDER BY
            occurrenceCount DESC,
            lastObservedAt DESC
        """
    )
    suspend fun getUnresolvedMistakesForEvidence(
        evidenceId: String
    ): List<DetectedMistakeEntity>

    /*
     * Returns mistakes related to a specific learner
     * message when the assessment evidence table references
     * messages through messageId.
     */
    @Query(
        """
        SELECT mistake.*
        FROM detected_mistakes AS mistake
        INNER JOIN assessment_evidence AS evidence
            ON evidence.id = mistake.evidenceId
        WHERE evidence.messageId = :messageId
        ORDER BY
            mistake.isResolved ASC,
            mistake.occurrenceCount DESC,
            mistake.lastObservedAt DESC
        """
    )
    suspend fun getMistakesForMessage(
        messageId: String
    ): List<DetectedMistakeEntity>

    /*
     * Checks whether one assessment produced any stored
     * mistakes.
     */
    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM detected_mistakes
            WHERE evidenceId = :evidenceId
        )
        """
    )
    suspend fun hasMistakesForEvidence(
        evidenceId: String
    ): Boolean


}

