package com.julesmeister.deutschcraft.feature.chat.domain.usecase

import com.julesmeister.deutschcraft.feature.chat.domain.AssistantReply
import com.julesmeister.deutschcraft.feature.chat.domain.MessageCorrection

data class SendChatMessageResult(
    val assistantReply: AssistantReply,
    val assessmentWarning: String? = null
)