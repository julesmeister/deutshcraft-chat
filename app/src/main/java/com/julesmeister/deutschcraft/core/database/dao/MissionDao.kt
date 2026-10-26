package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.julesmeister.deutschcraft.core.database.entity.mission.ConversationMissionEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveProgressEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionTemplateEntity
import com.julesmeister.deutschcraft.core.database.relation.ConversationMissionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {

    @Upsert
    suspend fun upsertMissionTemplate(
        template: MissionTemplateEntity
    )

    @Upsert
    suspend fun upsertMissionTemplates(
        templates: List<MissionTemplateEntity>
    )

    @Upsert
    suspend fun upsertMissionObjectives(
        objectives:
        List<MissionObjectiveEntity>
    )

    @Query(
        """
        SELECT *
        FROM mission_templates
        WHERE isEnabled = 1
        ORDER BY difficulty ASC, title ASC
        """
    )
    fun observeEnabledMissionTemplates():
            Flow<List<MissionTemplateEntity>>

    @Query(
        """
        SELECT *
        FROM mission_templates
        WHERE id = :missionTemplateId
        LIMIT 1
        """
    )
    suspend fun getMissionTemplate(
        missionTemplateId: String
    ): MissionTemplateEntity?

    @Query(
        """
        SELECT *
        FROM mission_objectives
        WHERE missionTemplateId = :missionTemplateId
        ORDER BY sequenceNumber ASC
        """
    )
    suspend fun getMissionObjectives(
        missionTemplateId: String
    ): List<MissionObjectiveEntity>

    @Upsert
    suspend fun upsertConversationMission(
        conversationMission:
        ConversationMissionEntity
    )

    @Upsert
    suspend fun upsertObjectiveProgress(
        progress:
        MissionObjectiveProgressEntity
    )

    @Upsert
    suspend fun upsertObjectiveProgressList(
        progress:
        List<MissionObjectiveProgressEntity>
    )

    @Transaction
    @Query(
        """
        SELECT *
        FROM conversation_missions
        WHERE conversationId = :conversationId
          AND status = 'ACTIVE'
        ORDER BY startedAt DESC, createdAt DESC
        LIMIT 1
        """
    )
    fun observeActiveMission(
        conversationId: String
    ): Flow<ConversationMissionWithDetails?>

    @Transaction
    @Query(
        """
        SELECT *
        FROM conversation_missions
        WHERE conversationId = :conversationId
        ORDER BY createdAt DESC
        """
    )
    fun observeConversationMissionHistory(
        conversationId: String
    ): Flow<List<ConversationMissionWithDetails>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM conversation_missions
        WHERE id = :conversationMissionId
        LIMIT 1
        """
    )
    suspend fun getConversationMission(
        conversationMissionId: String
    ): ConversationMissionWithDetails?

    @Query(
        """
        UPDATE conversation_missions
        SET status = :status,
            startedAt = :startedAt,
            updatedAt = :updatedAt
        WHERE id = :conversationMissionId
        """
    )
    suspend fun updateMissionStartState(
        conversationMissionId: String,
        status: String,
        startedAt: Long,
        updatedAt: Long
    ): Int

    @Query(
        """
        UPDATE conversation_missions
        SET status = :status,
            completedAt = :completedAt,
            updatedAt = :updatedAt
        WHERE id = :conversationMissionId
        """
    )
    suspend fun updateMissionCompletionState(
        conversationMissionId: String,
        status: String,
        completedAt: Long,
        updatedAt: Long
    ): Int

    @Query(
        """
        UPDATE conversation_missions
        SET status = :status,
            dismissedAt = :dismissedAt,
            updatedAt = :updatedAt
        WHERE id = :conversationMissionId
        """
    )
    suspend fun updateMissionDismissalState(
        conversationMissionId: String,
        status: String,
        dismissedAt: Long,
        updatedAt: Long
    ): Int

    @Query(
        """
        UPDATE mission_objective_progress
        SET isCompleted = 1,
            completedAt = :completedAt,
            completedByMessageId = :messageId,
            confidence = :confidence,
            updatedAt = :updatedAt
        WHERE conversationMissionId =
            :conversationMissionId
          AND objectiveId = :objectiveId
        """
    )
    suspend fun completeObjective(
        conversationMissionId: String,
        objectiveId: String,
        completedAt: Long,
        messageId: String,
        confidence: Float,
        updatedAt: Long
    ): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM mission_objectives AS objective
        INNER JOIN mission_objective_progress AS progress
            ON progress.objectiveId = objective.id
        WHERE progress.conversationMissionId = :conversationMissionId
          AND objective.isRequired = 1
          AND progress.isCompleted = 0
        """
    )
    suspend fun countIncompleteRequiredObjectives(
        conversationMissionId: String
    ): Int

    @Query(
        """
        DELETE FROM conversation_missions
        WHERE conversationId = :conversationId
        """
    )
    suspend fun deleteConversationMissions(
        conversationId: String
    )
}