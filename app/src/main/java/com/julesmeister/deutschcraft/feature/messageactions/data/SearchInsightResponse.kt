package com.julesmeister.deutschcraft.feature.messageactions.data

import kotlinx.serialization.Serializable

@Serializable
data class SentenceInsightResponse(
    val translation: String,
    val explanation: String,
    val simpleRephrase: String,
    val naturalRephrase: String,
    val advancedRephrase: String
)