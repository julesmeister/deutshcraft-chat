package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion

@Composable
fun ReplySuggestions(
    suggestions: List<ReplySuggestion>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = suggestions.isNotEmpty(),
        enter = fadeIn() + slideInVertically {
            it / 2
        },
        exit = fadeOut() + slideOutVertically {
            it / 2
        }
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(
                    horizontal = 12.dp,
                    vertical = 6.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { suggestion ->
                AssistChip(
                    onClick = {
                        onSuggestionClick(suggestion.id)
                    },
                    label = {
                        Text(text = suggestion.text)
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor =
                            MaterialTheme.colorScheme.secondaryContainer,
                        labelColor =
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
    }
}