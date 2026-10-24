package com.julesmeister.deutschcraft.core.cefr.engine

import kotlin.math.exp
import kotlin.math.roundToInt

internal fun Int.clampScore(): Int {
    return coerceIn(
        minimumValue =
            AssessmentConstants.MINIMUM_SCORE,
        maximumValue =
            AssessmentConstants.MAXIMUM_SCORE
    )
}

internal fun Double.clampConfidence(): Double {
    return coerceIn(
        minimumValue =
            AssessmentConstants.MINIMUM_CONFIDENCE,
        maximumValue =
            AssessmentConstants.MAXIMUM_CONFIDENCE
    )
}

internal fun calculateConfidence(
    evidenceCount: Int,
    sourceReliability: Double
): Double {
    if (evidenceCount <= 0) {
        return 0.0
    }

    val evidenceConfidence =
        1.0 - exp(
            -evidenceCount /
                    AssessmentConstants
                        .CONFIDENCE_EVIDENCE_TARGET
        )

    return (
            evidenceConfidence * sourceReliability
            ).clampConfidence()
}

internal fun weightedAverage(
    values: List<Pair<Int, Double>>
): Int {
    val positiveWeights = values.filter { value ->
        value.second > 0.0
    }

    if (positiveWeights.isEmpty()) {
        return 0
    }

    val weightedTotal = positiveWeights.sumOf {
            (score, weight) ->
        score.toDouble() * weight
    }

    val totalWeight = positiveWeights.sumOf {
            (_, weight) ->
        weight
    }

    return (weightedTotal / totalWeight)
        .roundToInt()
        .clampScore()
}