package com.julesmeister.deutschcraft.core.database.model

import com.julesmeister.deutschcraft.core.cefr.model.GrammarFeature

data class GrammarFeatureSummaryRow(
    val grammarFeature: GrammarFeature,
    val totalEvidenceCount: Int,
    val successfulEvidenceCount: Int,
    val averageConfidence: Double
) {
    val successRate: Double
        get() {
            if (totalEvidenceCount == 0) {
                return 0.0
            }

            return successfulEvidenceCount.toDouble() /
                    totalEvidenceCount.toDouble()
        }
}