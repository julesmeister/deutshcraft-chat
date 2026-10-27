package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionObjectiveHintUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionObjectiveHintUsageDao {

    /*
     * Records one revealed hint level for one objective.
     *
     * The entity uses this composite primary key:
     *
     * conversationMissionId
     * objectiveId
     * hintLevel
     *
     * Therefore, opening the same hint repeatedly does not
     * create duplicate analytics evidence.
     *
     * Room returns:
     *
     * Positive row ID:
     * The hint usage was inserted.
     *
     * -1:
     * The same hint level was already recorded and IGNORE
     * rejected the duplicate.
     */
    @Insert(
        onConflict =
            OnConflictStrategy.IGNORE
    )
    suspend fun insertHintUsage(
        usage:
        MissionObjectiveHintUsageEntity
    ): Long

    /*
     * Counts the number of unique hint levels revealed for
     * one mission objective.
     *
     * Expected result:
     *
     * 0 = no hints
     * 1 = one hint revealed
     * 2 = two hints revealed
     * 3 = all hints revealed
     */
    @Query(
        """
        SELECT COUNT(*)
        FROM mission_objective_hint_usage
        WHERE conversationMissionId =
            :conversationMissionId
          AND objectiveId =
            :objectiveId
        """
    )
    suspend fun countHintsUsed(
        conversationMissionId: String,
        objectiveId: String
    ): Int

    /*
     * Returns the strongest hint level revealed for one
     * objective.
     *
     * Expected result:
     *
     * 0 = no hint revealed
     * 1 = gentle hint
     * 2 = contextual or more specific hint
     * 3 = direct wording suggestion
     */
    @Query(
        """
        SELECT COALESCE(
            MAX(hintLevel),
            0
        )
        FROM mission_objective_hint_usage
        WHERE conversationMissionId =
            :conversationMissionId
          AND objectiveId =
            :objectiveId
        """
    )
    suspend fun getHighestHintLevel(
        conversationMissionId: String,
        objectiveId: String
    ): Int

    /*
     * Returns every hint revealed for one objective in hint
     * progression order.
     */
    @Query(
        """
        SELECT *
        FROM mission_objective_hint_usage
        WHERE conversationMissionId =
            :conversationMissionId
          AND objectiveId =
            :objectiveId
        ORDER BY hintLevel ASC
        """
    )
    suspend fun getObjectiveHintUsage(
        conversationMissionId: String,
        objectiveId: String
    ): List<MissionObjectiveHintUsageEntity>

    /*
     * Returns all hint evidence for one conversation mission.
     *
     * This query is used when building
     * MissionCompletionEvidence after mission completion.
     */
    @Query(
        """
        SELECT *
        FROM mission_objective_hint_usage
        WHERE conversationMissionId =
            :conversationMissionId
        ORDER BY revealedAt ASC
        """
    )
    suspend fun getMissionHintUsage(
        conversationMissionId: String
    ): List<MissionObjectiveHintUsageEntity>

    /*
     * Observes all hint usage for a mission.
     *
     * This can later support live analytics such as:
     *
     * - Total hints used
     * - Independent objectives
     * - Directly assisted objectives
     */
    @Query(
        """
        SELECT *
        FROM mission_objective_hint_usage
        WHERE conversationMissionId =
            :conversationMissionId
        ORDER BY revealedAt ASC
        """
    )
    fun observeMissionHintUsage(
        conversationMissionId: String
    ): Flow<List<MissionObjectiveHintUsageEntity>>

    /*
     * Returns the total number of unique hint levels revealed
     * across the complete mission.
     */
    @Query(
        """
        SELECT COUNT(*)
        FROM mission_objective_hint_usage
        WHERE conversationMissionId =
            :conversationMissionId
        """
    )
    suspend fun countMissionHintsUsed(
        conversationMissionId: String
    ): Int

    /*
     * Returns the number of mission objectives that used at
     * least one hint.
     */
    @Query(
        """
        SELECT COUNT(
            DISTINCT objectiveId
        )
        FROM mission_objective_hint_usage
        WHERE conversationMissionId =
            :conversationMissionId
        """
    )
    suspend fun countObjectivesUsingHints(
        conversationMissionId: String
    ): Int

    /*
     * Returns the number of objectives for which the learner
     * revealed the direct third hint.
     *
     * This later contributes to:
     *
     * directlyAssistedObjectiveCount
     */
    @Query(
        """
        SELECT COUNT(
            DISTINCT objectiveId
        )
        FROM mission_objective_hint_usage
        WHERE conversationMissionId =
            :conversationMissionId
          AND hintLevel >= 3
        """
    )
    suspend fun countDirectlyAssistedObjectives(
        conversationMissionId: String
    ): Int

    /*
     * Deletes hint evidence when its owning conversation
     * mission is permanently removed.
     *
     * This is currently explicit because the hint table does
     * not declare a foreign key to ConversationMissionEntity.
     */
    @Query(
        """
        DELETE FROM mission_objective_hint_usage
        WHERE conversationMissionId =
            :conversationMissionId
        """
    )
    suspend fun deleteMissionHintUsage(
        conversationMissionId: String
    ): Int
}