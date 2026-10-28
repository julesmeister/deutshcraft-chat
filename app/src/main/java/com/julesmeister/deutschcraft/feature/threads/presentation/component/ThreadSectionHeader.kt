package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.threads.presentation.theme.ThreadDesignTokens as Tokens

@Composable
fun ThreadSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    val colors = rememberThreadColors()

    Text(
        modifier = modifier.padding(
            start = Tokens.SectionHorizontalPadding,
            top = Tokens.SectionTopPadding,
            end = Tokens.SectionHorizontalPadding,
            bottom = Tokens.SectionBottomPadding
        ),
        text = title,
        color = colors.accent,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold
    )
}