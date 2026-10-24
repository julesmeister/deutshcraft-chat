package com.julesmeister.deutschcraft.feature.progress.domain

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

data class ProgressSummary(
    val currentLevel: CefrLevel,
    val readinessPercentage: Int,

    val grammarScore: Int,
    val vocabularyScore: Int,
    val coherenceScore: Int,
    val accuracyScore: Int,
    val rangeScore: Int,

    val overallScore: Int,

    val assessedMessageCount: Int,
    val activeDayCount: Int
)