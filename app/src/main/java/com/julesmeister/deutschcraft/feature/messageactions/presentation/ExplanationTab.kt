package com.julesmeister.deutschcraft.feature.messageactions.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ExplanationTab(
    explanation: String
) {
    Text(
        text =
            "💡 Explanation\n\n$explanation"
    )
}