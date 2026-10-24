package com.julesmeister.deutschcraft.core.database.model

import com.julesmeister.deutschcraft.core.cefr.model.GrammarFeature
import com.julesmeister.deutschcraft.core.cefr.model.MistakeCategory
import com.julesmeister.deutschcraft.core.cefr.model.MistakeSeverity

data class MistakeSummaryRow(
    val category: MistakeCategory,
    val grammarFeature: GrammarFeature?,
    val severity: MistakeSeverity,
    val totalOccurrences: Int,
    val latestOccurrenceAt: Long
)