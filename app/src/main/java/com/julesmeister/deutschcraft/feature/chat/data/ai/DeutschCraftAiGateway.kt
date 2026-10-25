package com.julesmeister.deutschcraft.feature.chat.data.ai

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage

interface DeutschCraftAiGateway {

    suspend fun generateReply(
        learnerMessage: String,
        conversationHistory: List<ChatMessage>,
        cefrLevel: CefrLevel
    ): AiReply
}