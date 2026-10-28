package com.julesmeister.deutschcraft.feature.settings.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.settings.domain.AppTheme
import com.julesmeister.deutschcraft.feature.settings.presentation.theme.SettingsDesignTokens as Tokens

@Composable
internal fun ThemePicker(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Tokens.ThemeCardSpacing)
    ) {
        AppTheme.entries.forEach { theme ->
            ThemeCard(
                theme = theme,
                selected = selectedTheme == theme,
                onClick = {
                    if (selectedTheme != theme) {
                        onThemeSelected(theme)
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeCard(
    theme: AppTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            colorScheme.primary
        } else {
            colorScheme.outlineVariant.copy(alpha = Tokens.ThemeCardRestingBorderAlpha)
        },
        animationSpec = tween(Tokens.StateAnimationMillis),
        label = "ThemeCardBorderColor"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (selected) {
            Tokens.ThemeCardBorderSelected
        } else {
            Tokens.ThemeCardBorderResting
        },
        animationSpec = tween(Tokens.StateAnimationMillis),
        label = "ThemeCardBorderWidth"
    )

    Column(
        modifier = modifier
            .clip(Tokens.ThemeCardShape)
            .background(colorScheme.surfaceContainerHigh)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = Tokens.ThemeCardShape
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(Tokens.ThemeCardPadding)
    ) {
        Box {
            ThemeMiniPreview(theme = theme)

            ThemeCheckBadge(
                visible = selected,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Tokens.ThemeCheckOffset)
            )
        }

        Row(
            modifier = Modifier.padding(top = Tokens.ThemeCardLabelTopPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (theme) {
                    AppTheme.DARK -> Icons.Outlined.DarkMode
                    AppTheme.LIGHT -> Icons.Outlined.LightMode
                },
                contentDescription = null,
                modifier = Modifier.size(Tokens.ThemeCardLabelIconSize),
                tint = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(Tokens.ThemeCardLabelIconSpacing))

            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ThemeCheckBadge(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(Tokens.ThemeCheckSize)
                .clip(CircleShape)
                .background(colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(Tokens.ThemeCheckIconSize),
                tint = colorScheme.onPrimary
            )
        }
    }
}

/*
 * Tiny mock conversation drawn in the theme's own colors.
 */
@Composable
private fun ThemeMiniPreview(
    theme: AppTheme
) {
    val (background, incoming, outgoing) = when (theme) {
        AppTheme.DARK -> Triple(
            Tokens.DarkPreviewBackground,
            Tokens.DarkPreviewIncoming,
            Tokens.DarkPreviewOutgoing
        )

        AppTheme.LIGHT -> Triple(
            Tokens.LightPreviewBackground,
            Tokens.LightPreviewIncoming,
            Tokens.LightPreviewOutgoing
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(Tokens.ThemePreviewHeight)
            .clip(Tokens.ThemePreviewShape)
            .background(background)
            .padding(Tokens.ThemePreviewPadding),
        verticalArrangement = Arrangement.spacedBy(
            space = Tokens.ThemePreviewBubbleSpacing,
            alignment = Alignment.Bottom
        )
    ) {
        PreviewBubble(color = incoming, widthFraction = Tokens.ThemePreviewIncomingWidth, alignEnd = false)
        PreviewBubble(color = outgoing, widthFraction = Tokens.ThemePreviewOutgoingWidth, alignEnd = true)
        PreviewBubble(color = incoming, widthFraction = Tokens.ThemePreviewShortWidth, alignEnd = false)
    }
}

@Composable
private fun PreviewBubble(
    color: Color,
    widthFraction: Float,
    alignEnd: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (alignEnd) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .height(Tokens.ThemePreviewBubbleHeight)
                .clip(Tokens.ThemePreviewBubbleShape)
                .background(color)
        )
    }
}