package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

/*
 * Theme-aware colors for the Progress screen.
 *
 * Dark mode reproduces the original look exactly.
 * Light mode swaps in readable text, a deeper accent, and an
 * inverted current-level tile.
 */
@Immutable
internal data class ProgressColors(
    val isDark: Boolean,
    val textPrimary: Color,
    val textSecondary: Color,

    /*
     * Accent for icons, counts, and progress bar fills.
     */
    val accent: Color,

    /*
     * Tinted background behind accent icons.
     */
    val accentContainer: Color,

    val progressTrack: Color,

    /*
     * Current-level tile in the CEFR bento.
     */
    val currentLevelBackground: Color,
    val currentLevelTextPrimary: Color,
    val currentLevelTextSecondary: Color
)

@Composable
internal fun rememberProgressColors(): ProgressColors {
    val colorScheme =
        MaterialTheme.colorScheme

    return remember(colorScheme) {
        val isDark =
            colorScheme.background.luminance() <
                    ProgressDarkLuminanceThreshold

        if (isDark) {
            ProgressColors(
                isDark = true,
                textPrimary = DeutschTextPrimary,
                textSecondary = DeutschTextSecondary,
                accent = DeutschLime,
                accentContainer = DeutschLime.copy(
                    alpha = ProgressDarkAccentContainerAlpha
                ),
                progressTrack = colorScheme.surfaceContainerHighest,
                currentLevelBackground = CefrCurrentLevelBackground,
                currentLevelTextPrimary = CefrCurrentLevelTextPrimary,
                currentLevelTextSecondary = CefrCurrentLevelTextSecondary
            )
        } else {
            ProgressColors(
                isDark = false,
                textPrimary = colorScheme.onSurface,
                textSecondary = colorScheme.onSurfaceVariant,
                accent = ProgressLightAccent,
                accentContainer = DeutschLime.copy(
                    alpha = ProgressLightAccentContainerAlpha
                ),
                progressTrack = colorScheme.surfaceContainerHighest,
                currentLevelBackground = colorScheme.inverseSurface,
                currentLevelTextPrimary = colorScheme.inverseOnSurface,
                currentLevelTextSecondary = colorScheme.inverseOnSurface.copy(
                    alpha = ProgressLightInverseSecondaryAlpha
                )
            )
        }
    }
}

private const val ProgressDarkLuminanceThreshold = 0.5f

/*
 * Matches the old per-component alphas for dark mode.
 */
private const val ProgressDarkAccentContainerAlpha = 0.14f

/*
 * Deeper olive-lime: readable as text and icons on white,
 * still clearly the DeutschCraft lime family.
 */
private val ProgressLightAccent = Color(0xFF5A7A00)

private const val ProgressLightAccentContainerAlpha = 0.35f

private const val ProgressLightInverseSecondaryAlpha = 0.72f