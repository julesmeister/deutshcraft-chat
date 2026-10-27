package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ThreadSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        modifier =
            modifier.padding(
                start =
                    SECTION_HORIZONTAL_PADDING,
                top =
                    SECTION_TOP_PADDING,
                end =
                    SECTION_HORIZONTAL_PADDING,
                bottom =
                    SECTION_BOTTOM_PADDING
            ),
        text =
            title,
        color =
            MaterialTheme
                .colorScheme
                .primary,
        style =
            MaterialTheme
                .typography
                .labelLarge,
        fontWeight =
            FontWeight.SemiBold
    )
}

private val SECTION_HORIZONTAL_PADDING =
    16.dp

private val SECTION_TOP_PADDING =
    18.dp

private val SECTION_BOTTOM_PADDING =
    6.dp