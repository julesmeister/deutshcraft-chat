package com.julesmeister.deutschcraft.feature.mission.data.ai

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjective

interface MissionObjectiveEvaluationAiGateway {

    suspend fun evaluateMessage(
        learnerMessage: String,
        recentConversation:
        List<MissionEvaluationTurn>,
        cefrLevel: CefrLevel,
        missionTitle: String,
        missionScenario: String,
        learnerRole: String,
        assistantRole: String,
        unfinishedObjectives:
        List<MissionObjective>
    ): MissionObjectiveEvaluationResult
}
