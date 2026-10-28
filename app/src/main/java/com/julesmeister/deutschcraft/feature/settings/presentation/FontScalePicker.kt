package com.julesmeister.deutschcraft.feature.settings.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.settings.domain.FontScaleOption
import com.julesmeister.deutschcraft.feature.settings.presentation.theme.SettingsDesignTokens as Tokens

@Composable
internal fun FontScalePicker(
    selectedOption: FontScaleOption,
    onOptionSelected: (FontScaleOption) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Tokens.FontPillSpacing)
        ) {
            FontScaleOption.entries.forEach { option ->
                FontScalePill(
                    option = option,
                    selected = selectedOption == option,
                    onClick = {
                        if (selectedOption != option) {
                            onOptionSelected(option)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        FontScalePreview(scale = selectedOption.scale)
    }
}

@Composable
private fun FontScalePill(
    option: FontScaleOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val background by animateColorAsState(
        targetValue = if (selected) colorScheme.primary else colorScheme.surfaceContainerHigh,
        animationSpec = tween(Tokens.StateAnimationMillis),
        label = "FontPillBackground"
    )

    val content by animateColorAsState(
        targetValue = if (selected) colorScheme.onPrimary else colorScheme.onSurface,
        animationSpec = tween(Tokens.StateAnimationMillis),
        label = "FontPillContent"
    )

    val border by animateColorAsState(
        targetValue = if (selected) {
            colorScheme.primary
        } else {
            colorScheme.outlineVariant.copy(alpha = Tokens.FontPillRestingBorderAlpha)
        },
        animationSpec = tween(Tokens.StateAnimationMillis),
        label = "FontPillBorder"
    )

    Box(
        modifier = modifier
            .height(Tokens.FontPillHeight)
            .clip(Tokens.FontPillShape)
            .background(background)
            .border(
                width = Tokens.FontPillBorder,
                color = border,
                shape = Tokens.FontPillShape
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .semantics { contentDescription = option.displayName },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = Tokens.FontPillLetter,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = MaterialTheme.typography.titleMedium.fontSize * option.scale
            ),
            fontWeight = FontWeight.Bold,
            color = content
        )
    }
}

/*
 * Live preview styled like an incoming chat bubble.
 */
@Composable
private fun FontScalePreview(
    scale: Float
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Tokens.FontPreviewTopPadding)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(Tokens.FontPreviewWidthFraction)
                .clip(Tokens.FontPreviewShape)
                .background(colorScheme.surfaceContainerHighest)
                .animateContentSize(animationSpec = tween(Tokens.StateAnimationMillis))
                .padding(
                    horizontal = Tokens.FontPreviewHorizontalPadding,
                    vertical = Tokens.FontPreviewVerticalPadding
                )
        ) {
            Text(
                text = Tokens.FontPreviewSentence,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * scale
                ),
                color = colorScheme.onSurface
            )
        }
    }
}