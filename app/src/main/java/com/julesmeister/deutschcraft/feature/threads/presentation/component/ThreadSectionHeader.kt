package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime

@Composable
fun ThreadSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier.padding(
            start = 16.dp,
            top = 18.dp,
            end = 16.dp,
            bottom = 6.dp
        ),
        text = title,
        color = DeutschLime,
        style =
            MaterialTheme.typography
                .labelLarge,
        fontWeight = FontWeight.SemiBold
    )
}