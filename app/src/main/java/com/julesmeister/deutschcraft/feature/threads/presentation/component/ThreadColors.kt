package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.julesmeister.deutschcraft.feature.threads.presentation.theme.ThreadDesignTokens as Tokens
import com.julesmeister.deutschcraft.ui.theme.DeutschError
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschSurface
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHigh
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@Immutable
internal data class ThreadColors(
    val isDark: Boolean,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val accentContainer: Color,
    val dialogSurface: Color,
    val closeButtonSurface: Color,
    val error: Color,
    val errorContainer: Color
)

@Composable
internal fun rememberThreadColors(): ThreadColors {
    val colorScheme = MaterialTheme.colorScheme

    return remember(colorScheme) {
        val isDark =
            colorScheme.background.luminance() < Tokens.DarkLuminanceThreshold

        if (isDark) {
            ThreadColors(
                isDark = true,
                textPrimary = DeutschTextPrimary,
                textSecondary = DeutschTextSecondary,
                accent = DeutschLime,
                accentContainer = DeutschLime.copy(alpha = Tokens.DarkAccentContainerAlpha),
                dialogSurface = DeutschSurfaceHigh,
                closeButtonSurface = DeutschSurface,
                error = DeutschError,
                errorContainer = DeutschError.copy(alpha = Tokens.DarkErrorContainerAlpha)
            )
        } else {
            ThreadColors(
                isDark = false,
                textPrimary = colorScheme.onSurface,
                textSecondary = colorScheme.onSurfaceVariant,
                accent = Tokens.LightAccent,
                accentContainer = DeutschLime.copy(alpha = Tokens.LightAccentContainerAlpha),
                dialogSurface = colorScheme.surfaceContainerHigh,
                closeButtonSurface = colorScheme.surfaceContainerHighest,
                error = colorScheme.error,
                errorContainer = colorScheme.error.copy(alpha = Tokens.LightErrorContainerAlpha)
            )
        }
    }
}