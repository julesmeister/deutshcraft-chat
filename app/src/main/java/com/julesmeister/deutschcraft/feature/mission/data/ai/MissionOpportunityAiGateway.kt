package com.julesmeister.deutschcraft.feature.mission.data.ai

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage

/*
 * AI boundary dedicated to detecting contextual
 * conversation-mission opportunities.
 *
 * The returned value is still an untrusted data-layer
 * model. DetectMissionOpportunityUseCase validates it
 * before converting it into a MissionOpportunity.
 */
interface MissionOpportunityAiGateway {

    suspend fun detectMissionOpportunity(
        conversationHistory:
        List<ChatMessage>,
        cefrLevel:
        CefrLevel
    ): AiMissionOpportunity
}