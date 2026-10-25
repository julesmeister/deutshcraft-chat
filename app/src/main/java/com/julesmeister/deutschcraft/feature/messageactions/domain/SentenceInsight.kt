package com.julesmeister.deutschcraft.feature.messageactions.domain

import kotlinx.serialization.Serializable

@Serializable
data class SentenceInsight(
    val translation: String,
    val explanation: String,
    val simpleRephrase: String,
    val naturalRephrase: String,
    val advancedRephrase: String
)