package com.julesmeister.deutschcraft.feature.messageactions.presentation

import com.julesmeister.deutschcraft.feature.messageactions.domain.SentenceAnalysisType
import com.julesmeister.deutschcraft.feature.messageactions.domain.SentenceInsight

data class MessageActionState(
    val selectedAction: SentenceAnalysisType? = null,
    val analyzedSentence: String? = null,
    val insight: SentenceInsight? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)