package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AssistantStatus(
    isAssistantTyping: Boolean,
    isAssessing: Boolean,
    modifier: Modifier = Modifier
) {
    val statusText =
        when {
            isAssistantTyping -> {
                "schreibt gerade"
            }

            isAssessing -> {
                "analysiert"
            }

            else -> {
                "online"
            }
        }

    Row(
        modifier =
            modifier,
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                STATUS_SPACING
            )
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        STATUS_DOT_SIZE
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .primary
                    )
        )

        Text(
            text =
                statusText,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
            style =
                MaterialTheme
                    .typography
                    .labelMedium
                    .copy(
                        fontSize =
                            STATUS_FONT_SIZE,
                        lineHeight =
                            STATUS_LINE_HEIGHT
                    ),
            fontWeight =
                FontWeight.Normal,
            maxLines =
                1
        )
    }
}

private val STATUS_SPACING =
    5.dp

private val STATUS_DOT_SIZE =
    6.dp

private val STATUS_FONT_SIZE =
    11.sp

private val STATUS_LINE_HEIGHT =
    13.sp