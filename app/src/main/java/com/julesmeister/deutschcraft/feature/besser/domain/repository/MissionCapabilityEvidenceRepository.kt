// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/domain/repository/MissionCapabilityEvidenceRepository.kt

package com.julesmeister.deutschcraft.feature.besser.domain.repository

import com.julesmeister.deutschcraft.feature.besser.domain.mastery.MissionCapabilityEvidence
import kotlinx.coroutines.flow.Flow

interface MissionCapabilityEvidenceRepository {

    suspend fun save(
        evidence:
        MissionCapabilityEvidence
    )

    suspend fun saveAll(
        evidence:
        List<MissionCapabilityEvidence>
    )

    suspend fun getForMission(
        missionId: String
    ): List<MissionCapabilityEvidence>

    fun observeForCapability(
        capabilityId: String
    ): Flow<List<MissionCapabilityEvidence>>

    suspend fun getForCapabilities(
        capabilityIds:
        Collection<String>
    ): Map<String, List<MissionCapabilityEvidence>>

    suspend fun getRecentForCapability(
        capabilityId: String,
        limit: Int
    ): List<MissionCapabilityEvidence>

    suspend fun deleteForMission(
        missionId: String
    )

    suspend fun deleteForConversation(
        conversationId: String
    )
}