package com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository

import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionMessageInteractionEvidence
import kotlinx.coroutines.flow.Flow

interface MissionInteractionEvidenceRepository {

    suspend fun save(
        evidence:
        MissionMessageInteractionEvidence
    )

    suspend fun getForMission(
        conversationMissionId: String
    ): List<MissionMessageInteractionEvidence>

    suspend fun getForMessage(
        messageId: String
    ): MissionMessageInteractionEvidence?

    fun observeForMission(
        conversationMissionId: String
    ): Flow<List<MissionMessageInteractionEvidence>>

    suspend fun deleteForMission(
        conversationMissionId: String
    )
}