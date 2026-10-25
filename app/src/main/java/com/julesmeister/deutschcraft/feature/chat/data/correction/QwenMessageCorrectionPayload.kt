package com.julesmeister.deutschcraft.feature.chat.data.correction

import kotlinx.serialization.Serializable

@Serializable
data class QwenMessageCorrectionPayload(
    val needsCorrection: Boolean,
    val correctedSentence: String? = null
)