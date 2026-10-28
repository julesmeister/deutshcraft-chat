package com.julesmeister.deutschcraft.feature.settings.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.settings.presentation.theme.SettingsDesignTokens as Tokens

@Composable
internal fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            modifier = Modifier.padding(
                start = Tokens.SectionTitleStartPadding,
                bottom = Tokens.SectionTitleBottomPadding
            ),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Tokens.SectionShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(vertical = Tokens.SectionVerticalPadding)
        ) {
            content()
        }
    }
}

/*
 * Icon + title + description header with custom content
 * underneath (theme picker, font picker).
 */
@Composable
internal fun SettingsLabeledBlock(
    icon: ImageVector,
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(Tokens.RowPadding)
    ) {
        SettingsRowLabel(
            icon = icon,
            title = title,
            description = description,
            isActive = true
        )

        Spacer(modifier = Modifier.height(Tokens.RowPadding))

        content()
    }
}

@Composable
internal fun SettingsRowLabel(
    icon: ImageVector,
    title: String,
    description: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconTile(
            icon = icon,
            isActive = isActive
        )

        Spacer(modifier = Modifier.width(Tokens.IconSpacing))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                modifier = Modifier.padding(top = Tokens.DescriptionTopPadding),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun SettingsIconTile(
    icon: ImageVector,
    isActive: Boolean
) {
    val primary = MaterialTheme.colorScheme.primary

    val background by animateColorAsState(
        targetValue = primary.copy(
            alpha = if (isActive) {
                Tokens.IconActiveBackgroundAlpha
            } else {
                Tokens.IconBackgroundAlpha
            }
        ),
        animationSpec = tween(Tokens.StateAnimationMillis),
        label = "SettingsIconBackground"
    )

    Box(
        modifier = Modifier
            .size(Tokens.IconSize)
            .clip(Tokens.IconShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primary
        )
    }
}

/*
 * Inset divider aligned with the row text, not the icon.
 */
@Composable
internal fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Tokens.DividerStartPadding,
                end = Tokens.DividerEndPadding
            )
            .height(Tokens.DividerHeight)
            .background(
                MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = Tokens.DividerAlpha
                )
            )
    )
}