package com.julesmeister.deutschcraft.feature.mission.domain.repository

import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionTemplate
import kotlinx.coroutines.flow.Flow

interface MissionRepository {

    fun observeMissionCatalog():
            Flow<List<MissionTemplate>>

    fun observeActiveMission(
        conversationId: String
    ): Flow<ConversationMission?>

    fun observeMissionHistory(
        conversationId: String
    ): Flow<List<ConversationMission>>

    fun observeCompletedMissions():
            Flow<List<ConversationMission>>


    suspend fun getMissionTemplate(
        missionTemplateId: String
    ): MissionTemplate?

    suspend fun getConversationMission(
        conversationMissionId: String
    ): ConversationMission?

    suspend fun saveMissionTemplate(
        template: MissionTemplate
    )

    suspend fun offerMission(
        conversationId: String,
        missionTemplateId: String
    ): String

    suspend fun startMission(
        conversationMissionId: String
    )

    suspend fun dismissMission(
        conversationMissionId: String
    )

    suspend fun completeObjective(
        conversationMissionId: String,
        objectiveId: String,
        messageId: String,
        confidence: Float
    )

    suspend fun completeMission(
        conversationMissionId: String
    )

    suspend fun deleteConversationMissions(
        conversationId: String
    )
}