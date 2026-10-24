package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschSurface
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary

@Composable
fun ReplySuggestions(
    suggestions: List<ReplySuggestion>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = suggestions.isNotEmpty(),
        enter = fadeIn() + scaleIn(
            initialScale = 0.94f
        ),
        exit = fadeOut() + scaleOut(
            targetScale = 0.94f
        )
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 12.dp,
                    vertical = 6.dp
                ),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { suggestion ->
                AssistChip(
                    onClick = {
                        onSuggestionClick(
                            suggestion.id
                        )
                    },
                    label = {
                        Text(
                            text = suggestion.text,
                            maxLines = 1
                        )
                    },
                    shape =
                        RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color =
                            DeutschLime.copy(
                                alpha = 0.7f
                            )
                    ),
                    colors =
                        AssistChipDefaults
                            .assistChipColors(
                                containerColor =
                                    DeutschSurface,
                                labelColor =
                                    DeutschTextPrimary
                            )
                )
            }
        }
    }
}