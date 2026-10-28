package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatGlassColors

@Composable
internal fun BesserRefinementHeader(
    instruction: String,
    enabled: Boolean,
    glassColors: ChatGlassColors,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme =
        MaterialTheme.colorScheme

    val cancelInteractionSource =
        remember {
            MutableInteractionSource()
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(
                    ChatDesignTokens
                        .RefinementShape
                )
                .background(
                    brush =
                        glassColors.headerBrush
                )
                .border(
                    width =
                        ChatDesignTokens
                            .RefinementBorderWidth,
                    brush =
                        glassColors.headerBorderBrush,
                    shape =
                        ChatDesignTokens
                            .RefinementShape
                )
                .padding(
                    horizontal =
                        ChatDesignTokens
                            .RefinementContentHorizontalPadding,
                    vertical =
                        ChatDesignTokens
                            .RefinementContentVerticalPadding
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        ChatDesignTokens
                            .RefinementIconContainerSize
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        glassColors.headerIconBackground
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.AutoAwesome,
                contentDescription =
                    null,
                modifier =
                    Modifier.size(
                        ChatDesignTokens
                            .RefinementIconSize
                    ),
                tint =
                    colorScheme.primary
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    ChatDesignTokens
                        .RefinementIconTextSpacing
                )
        )

        Column(
            modifier =
                Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(
                    ChatDesignTokens
                        .RefinementTextSpacing
                )
        ) {
            Text(
                text =
                    "Besser",
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.Bold,
                color =
                    colorScheme.primary
            )

            Text(
                text =
                    instruction,
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    colorScheme.onSurfaceVariant,
                maxLines =
                    ChatDesignTokens
                        .RefinementInstructionMaxLines,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    ChatDesignTokens
                        .RefinementCancelSpacing
                )
        )

        Box(
            modifier =
                Modifier
                    .size(
                        ChatDesignTokens
                            .RefinementCancelSize
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        glassColors.headerCancelBackground
                    )
                    .clickable(
                        interactionSource =
                            cancelInteractionSource,
                        indication =
                            null,
                        enabled =
                            enabled,
                        role =
                            Role.Button,
                        onClick =
                            onCancel
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Close,
                contentDescription =
                    "Cancel improvement",
                modifier =
                    Modifier.size(
                        ChatDesignTokens
                            .RefinementCancelIconSize
                    ),
                tint =
                    if (enabled) {
                        colorScheme.onSurfaceVariant
                    } else {
                        colorScheme
                            .onSurfaceVariant
                            .copy(
                                alpha =
                                    ChatDesignTokens
                                        .SendDisabledContentAlpha
                            )
                    }
            )
        }
    }
}