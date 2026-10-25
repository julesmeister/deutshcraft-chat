package com.julesmeister.deutschcraft.feature.messageactions.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.julesmeister.deutschcraft.feature.messageactions.domain.SentenceInsight

@Composable
fun RephraseTab(
    insight: SentenceInsight
) {
    Text(
        text =
            """
✍️ Rephrase

Simple:
${insight.simpleRephrase}

Natural:
${insight.naturalRephrase}

Advanced:
${insight.advancedRephrase}
            """.trimIndent()
    )
}