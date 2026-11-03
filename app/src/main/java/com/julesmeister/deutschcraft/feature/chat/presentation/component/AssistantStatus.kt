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
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens

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
                ChatDesignTokens.AssistantStatus.StatusSpacing
            )
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        ChatDesignTokens.AssistantStatus.StatusDotSize
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
                            ChatDesignTokens.AssistantStatus.StatusFontSize,
                        lineHeight =
                            ChatDesignTokens.AssistantStatus.StatusLineHeight
                    ),
            fontWeight =
                FontWeight.Normal,
            maxLines =
                1
        )
    }
}
