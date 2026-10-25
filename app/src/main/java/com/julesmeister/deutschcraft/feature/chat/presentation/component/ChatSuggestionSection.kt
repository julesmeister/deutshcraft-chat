package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion

@Composable
fun ChatSuggestionSection(
    suggestions: List<ReplySuggestion>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty()) {
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color =
                    MaterialTheme.colorScheme.background
            )
            .padding(
                top = 6.dp,
                bottom = 4.dp
            )
    ) {
        Text(
            text = "Suggested replies",
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 4.dp
            ),
            style =
                MaterialTheme.typography.labelMedium,
            fontWeight =
                FontWeight.Medium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        ReplySuggestions(
            suggestions = suggestions,
            onSuggestionClick = { suggestionId ->
                onSuggestionClick(
                    suggestionId
                )
            }
        )
    }
}