package com.julesmeister.deutschcraft.core.database.model

data class AssessmentScoreAverageRow(
    val grammarAverage: Double,
    val vocabularyAverage: Double,
    val coherenceAverage: Double,
    val accuracyAverage: Double,
    val rangeAverage: Double,
    val assessmentCount: Int
)