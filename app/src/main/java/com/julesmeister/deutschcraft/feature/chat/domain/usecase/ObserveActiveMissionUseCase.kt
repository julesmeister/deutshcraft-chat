package com.julesmeister.deutschcraft.feature.chat.domain.usecase

import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.repository.MissionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveActiveMissionUseCase @Inject constructor(
    private val missionRepository:
    MissionRepository
) {

    operator fun invoke(
        conversationId: String
    ): Flow<ConversationMission?> {
        return missionRepository.observeActiveMission(
            conversationId = conversationId
        )
    }
}