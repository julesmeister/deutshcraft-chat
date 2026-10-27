package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionInteractionEvidenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionInteractionEvidenceDao {

    /*
     * Re-evaluating the same learner message replaces the
     * existing result instead of creating duplicate evidence.
     */
    @Upsert
    suspend fun upsertInteractionEvidence(
        evidence:
        MissionInteractionEvidenceEntity
    )

    @Query(
        """
        SELECT *
        FROM mission_interaction_evidence
        WHERE conversationMissionId =
            :conversationMissionId
        ORDER BY evaluatedAt ASC
        """
    )
    suspend fun getMissionInteractionEvidence(
        conversationMissionId: String
    ): List<MissionInteractionEvidenceEntity>

    @Query(
        """
        SELECT *
        FROM mission_interaction_evidence
        WHERE conversationMissionId =
            :conversationMissionId
        ORDER BY evaluatedAt ASC
        """
    )
    fun observeMissionInteractionEvidence(
        conversationMissionId: String
    ): Flow<List<MissionInteractionEvidenceEntity>>

    @Query(
        """
        SELECT *
        FROM mission_interaction_evidence
        WHERE messageId = :messageId
        LIMIT 1
        """
    )
    suspend fun getInteractionEvidenceForMessage(
        messageId: String
    ): MissionInteractionEvidenceEntity?

    @Query(
        """
        SELECT COUNT(*)
        FROM mission_interaction_evidence
        WHERE conversationMissionId =
            :conversationMissionId
          AND replyExpected = 1
        """
    )
    suspend fun countExpectedReplies(
        conversationMissionId: String
    ): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM mission_interaction_evidence
        WHERE conversationMissionId =
            :conversationMissionId
          AND replyExpected = 1
          AND relevantReply = 1
          AND confidence >= :minimumConfidence
        """
    )
    suspend fun countRelevantReplies(
        conversationMissionId: String,
        minimumConfidence: Float
    ): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM mission_interaction_evidence
        WHERE conversationMissionId =
            :conversationMissionId
          AND followUpQuestion = 1
          AND confidence >= :minimumConfidence
        """
    )
    suspend fun countFollowUpQuestions(
        conversationMissionId: String,
        minimumConfidence: Float
    ): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM mission_interaction_evidence
        WHERE conversationMissionId =
            :conversationMissionId
          AND clarification = 1
          AND confidence >= :minimumConfidence
        """
    )
    suspend fun countClarifications(
        conversationMissionId: String,
        minimumConfidence: Float
    ): Int

    @Query(
        """
        DELETE FROM mission_interaction_evidence
        WHERE conversationMissionId =
            :conversationMissionId
        """
    )
    suspend fun deleteMissionInteractionEvidence(
        conversationMissionId: String
    ): Int
}