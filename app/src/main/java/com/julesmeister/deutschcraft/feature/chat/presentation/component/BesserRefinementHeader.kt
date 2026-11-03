package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.CircularProgressIndicator
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
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens

@Composable
internal fun BesserRefinementHeader(
    instruction: String,
    feedback: String?,
    isEvaluating: Boolean,
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
                .animateContentSize()
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
            if (isEvaluating) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(
                            ChatDesignTokens
                                .RefinementIconSize
                        ),
                    color =
                        colorScheme.primary,
                    strokeWidth =
                        ChatDesignTokens.RefinementFeedback.EvaluatingStrokeWidth
                )
            } else {
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
                    if (isEvaluating) {
                        ChatPresentationTokens.BesserRefinementHeader.EvaluatingLabel
                    } else {
                        ChatPresentationTokens.BesserRefinementHeader.TitleLabel
                    },
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

            AnimatedVisibility(
                visible =
                    !feedback.isNullOrBlank() &&
                            !isEvaluating,
                enter =
                    fadeIn() +
                            expandVertically(),
                exit =
                    fadeOut() +
                            shrinkVertically()
            ) {
                Text(
                    text =
                        feedback.orEmpty(),
                    modifier =
                        Modifier.padding(
                            top =
                                ChatDesignTokens.RefinementFeedback.FeedbackTopPadding
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        colorScheme.tertiary,
                    maxLines =
                        ChatDesignTokens.RefinementFeedback.FeedbackMaxLines,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }
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
