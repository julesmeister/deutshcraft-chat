package com.julesmeister.deutschcraft.feature.messageactions.data

import com.julesmeister.deutschcraft.feature.messageactions.domain.SentenceInsight

interface SentenceInsightGateway {

    suspend fun analyzeSentence(
        sentence: String
    ): SentenceInsight
}