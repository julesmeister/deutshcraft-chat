package com.julesmeister.deutschcraft.feature.messageactions.data

import kotlinx.serialization.Serializable

@Serializable
data class SentenceInsightRequest(
    val sentence: String
)