// app/src/main/java/com/julesmeister/deutschcraft/core/database/dao/MissionCapabilityEvidenceDao.kt

package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.julesmeister.deutschcraft.core.database.entity.besser.MissionCapabilityEvidenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionCapabilityEvidenceDao {

    @Upsert
    suspend fun upsert(
        evidence:
        MissionCapabilityEvidenceEntity
    )

    @Upsert
    suspend fun upsertAll(
        evidence:
        List<MissionCapabilityEvidenceEntity>
    )

    @Query(
        """
        SELECT *
        FROM mission_capability_evidence
        WHERE missionId = :missionId
        ORDER BY capabilityId ASC
        """
    )
    suspend fun getForMission(
        missionId: String
    ): List<MissionCapabilityEvidenceEntity>

    @Query(
        """
        SELECT *
        FROM mission_capability_evidence
        WHERE capabilityId = :capabilityId
        ORDER BY evaluatedAt DESC
        """
    )
    fun observeForCapability(
        capabilityId: String
    ): Flow<List<MissionCapabilityEvidenceEntity>>

    @Query(
        """
        SELECT *
        FROM mission_capability_evidence
        WHERE capabilityId IN (:capabilityIds)
        ORDER BY evaluatedAt DESC
        """
    )
    suspend fun getForCapabilities(
        capabilityIds: List<String>
    ): List<MissionCapabilityEvidenceEntity>

    @Query(
        """
        SELECT *
        FROM mission_capability_evidence
        WHERE capabilityId = :capabilityId
        ORDER BY evaluatedAt DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentForCapability(
        capabilityId: String,
        limit: Int
    ): List<MissionCapabilityEvidenceEntity>

    @Query(
        """
        DELETE FROM mission_capability_evidence
        WHERE missionId = :missionId
        """
    )
    suspend fun deleteForMission(
        missionId: String
    ): Int

    @Query(
        """
        DELETE FROM mission_capability_evidence
        WHERE conversationId = :conversationId
        """
    )
    suspend fun deleteForConversation(
        conversationId: String
    ): Int
}