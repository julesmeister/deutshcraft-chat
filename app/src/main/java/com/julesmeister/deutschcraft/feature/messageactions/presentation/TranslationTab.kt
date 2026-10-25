package com.julesmeister.deutschcraft.feature.messageactions.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun TranslationTab(
    translation: String
) {
    Text(
        text =
            "📖 Translation\n\n$translation"
    )
}