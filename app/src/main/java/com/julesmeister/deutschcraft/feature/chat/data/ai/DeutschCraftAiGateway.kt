package com.julesmeister.deutschcraft.feature.chat.data.ai

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.mission.domain.ActiveMissionConversationContext

interface DeutschCraftAiGateway {

    suspend fun generateReply(
        learnerMessage: String,
        conversationHistory: List<ChatMessage>,
        cefrLevel: CefrLevel,
        activeMissionContext:
        ActiveMissionConversationContext? =
            null
    ): AiReply
}