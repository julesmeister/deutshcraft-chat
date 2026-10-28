package com.julesmeister.deutschcraft.feature.chat.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp

/*
 * Theme-aware colors for the liquid-glass composer and the
 * Besser refinement header.
 *
 * Dark mode keeps the original translucent glass.
 * Light mode uses a near-white surface, a faint dark border,
 * and a soft shadow, because white highlights disappear on
 * light backgrounds.
 */
@Immutable
data class ChatGlassColors(
    val isDark: Boolean,

    val containerTop: Color,
    val containerMiddle: Color,
    val containerBottom: Color,

    val innerHighlightEdge: Color,
    val innerHighlightCenter: Color,

    val borderHighlight: Color,
    val borderResting: Color,
    val borderFocused: Color,
    val borderRefinement: Color,
    val borderBottom: Color,

    val shadowElevation: Dp,
    val shadowColor: Color,

    val placeholder: Color,

    val headerTop: Color,
    val headerBottom: Color,
    val headerBorderTop: Color,
    val headerBorderBottom: Color,
    val headerIconBackground: Color,
    val headerCancelBackground: Color
) {

    val containerBrush: Brush
        get() =
            Brush.verticalGradient(
                colors =
                    listOf(
                        containerTop,
                        containerMiddle,
                        containerBottom
                    )
            )

    val innerHighlightBrush: Brush
        get() =
            Brush.horizontalGradient(
                colors =
                    listOf(
                        innerHighlightEdge,
                        innerHighlightCenter,
                        Color.Transparent
                    )
            )

    val headerBrush: Brush
        get() =
            Brush.verticalGradient(
                colors =
                    listOf(
                        headerTop,
                        headerBottom
                    )
            )

    val headerBorderBrush: Brush
        get() =
            Brush.verticalGradient(
                colors =
                    listOf(
                        headerBorderTop,
                        headerBorderBottom
                    )
            )

    /*
     * The border reaches its state color early and then fades
     * almost completely, so the bottom edge stays quiet.
     */
    fun borderBrush(
        stateColor: Color
    ): Brush {
        return Brush.verticalGradient(
            colorStops =
                arrayOf(
                    0f to
                            borderHighlight,

                    ChatDesignTokens.GlassBorderStateStop to
                            stateColor,

                    1f to
                            borderBottom
                )
        )
    }
}

@Composable
fun rememberChatGlassColors(): ChatGlassColors {
    val colorScheme =
        MaterialTheme.colorScheme

    return remember(
        colorScheme
    ) {
        val isDark =
            colorScheme.background.luminance() <
                    ChatDesignTokens.DarkLuminanceThreshold

        if (isDark) {
            darkGlassColors(
                colorScheme =
                    colorScheme
            )
        } else {
            lightGlassColors(
                colorScheme =
                    colorScheme
            )
        }
    }
}

private fun darkGlassColors(
    colorScheme: ColorScheme
): ChatGlassColors {
    return ChatGlassColors(
        isDark =
            true,

        containerTop =
            colorScheme.surfaceContainerHigh.copy(
                alpha =
                    ChatDesignTokens.GlassTopAlpha
            ),
        containerMiddle =
            colorScheme.surface.copy(
                alpha =
                    ChatDesignTokens.GlassMiddleAlpha
            ),
        containerBottom =
            colorScheme.surfaceContainer.copy(
                alpha =
                    ChatDesignTokens.GlassBottomAlpha
            ),

        innerHighlightEdge =
            Color.White.copy(
                alpha =
                    ChatDesignTokens.GlassHighlightEdgeAlpha
            ),
        innerHighlightCenter =
            Color.White.copy(
                alpha =
                    ChatDesignTokens.GlassHighlightCenterAlpha
            ),

        borderHighlight =
            Color.White.copy(
                alpha =
                    ChatDesignTokens.GlassBorderHighlightAlpha
            ),
        borderResting =
            colorScheme.outlineVariant.copy(
                alpha =
                    ChatDesignTokens.GlassRestingBorderAlpha
            ),
        borderFocused =
            colorScheme.primary.copy(
                alpha =
                    ChatDesignTokens.GlassFocusedBorderAlpha
            ),
        borderRefinement =
            colorScheme.primary.copy(
                alpha =
                    ChatDesignTokens.RefinementBorderAlpha
            ),
        borderBottom =
            colorScheme.outline.copy(
                alpha =
                    ChatDesignTokens.GlassBorderBottomAlpha
            ),

        shadowElevation =
            ChatDesignTokens.GlassShadowElevation,
        shadowColor =
            Color.Transparent,

        placeholder =
            colorScheme.onSurfaceVariant.copy(
                alpha =
                    ChatDesignTokens.PlaceholderAlpha
            ),

        headerTop =
            colorScheme.surfaceContainerHigh.copy(
                alpha =
                    ChatDesignTokens.RefinementBackgroundAlpha
            ),
        headerBottom =
            colorScheme.surfaceContainer.copy(
                alpha =
                    ChatDesignTokens.RefinementBackgroundAlpha
            ),
        headerBorderTop =
            colorScheme.primary.copy(
                alpha =
                    ChatDesignTokens.RefinementBorderAlpha
            ),
        headerBorderBottom =
            colorScheme.outlineVariant.copy(
                alpha =
                    ChatDesignTokens.RefinementHeaderBorderAlpha
            ),
        headerIconBackground =
            colorScheme.primary.copy(
                alpha =
                    ChatDesignTokens.RefinementIconBackgroundAlpha
            ),
        headerCancelBackground =
            colorScheme.surfaceContainerHighest.copy(
                alpha =
                    ChatDesignTokens.RefinementCancelBackgroundAlpha
            )
    )
}

private fun lightGlassColors(
    colorScheme: ColorScheme
): ChatGlassColors {
    return ChatGlassColors(
        isDark =
            false,

        containerTop =
            colorScheme.surface.copy(
                alpha =
                    ChatDesignTokens.GlassLightTopAlpha
            ),
        containerMiddle =
            colorScheme.surface.copy(
                alpha =
                    ChatDesignTokens.GlassLightMiddleAlpha
            ),
        containerBottom =
            colorScheme.surfaceContainerLow.copy(
                alpha =
                    ChatDesignTokens.GlassLightBottomAlpha
            ),

        innerHighlightEdge =
            Color.White.copy(
                alpha =
                    ChatDesignTokens.GlassLightHighlightEdgeAlpha
            ),
        innerHighlightCenter =
            Color.White.copy(
                alpha =
                    ChatDesignTokens.GlassLightHighlightCenterAlpha
            ),

        borderHighlight =
            Color.White.copy(
                alpha =
                    ChatDesignTokens.GlassLightBorderHighlightAlpha
            ),
        borderResting =
            colorScheme.onSurface.copy(
                alpha =
                    ChatDesignTokens.GlassLightRestingBorderAlpha
            ),
        borderFocused =
            colorScheme.primary.copy(
                alpha =
                    ChatDesignTokens.GlassLightFocusedBorderAlpha
            ),
        borderRefinement =
            colorScheme.primary.copy(
                alpha =
                    ChatDesignTokens.RefinementLightBorderAlpha
            ),
        borderBottom =
            colorScheme.onSurface.copy(
                alpha =
                    ChatDesignTokens.GlassLightBorderBottomAlpha
            ),

        shadowElevation =
            ChatDesignTokens.GlassLightShadowElevation,
        shadowColor =
            colorScheme.onSurface.copy(
                alpha =
                    ChatDesignTokens.GlassLightShadowAlpha
            ),

        placeholder =
            colorScheme.onSurfaceVariant.copy(
                alpha =
                    ChatDesignTokens.GlassLightPlaceholderAlpha
            ),

        headerTop =
            colorScheme.surface.copy(
                alpha =
                    ChatDesignTokens.RefinementLightBackgroundAlpha
            ),
        headerBottom =
            colorScheme.surfaceContainerLow.copy(
                alpha =
                    ChatDesignTokens.RefinementLightBackgroundAlpha
            ),
        headerBorderTop =
            colorScheme.primary.copy(
                alpha =
                    ChatDesignTokens.RefinementLightBorderAlpha
            ),
        headerBorderBottom =
            colorScheme.onSurface.copy(
                alpha =
                    ChatDesignTokens.RefinementLightHeaderBorderAlpha
            ),
        headerIconBackground =
            colorScheme.primary.copy(
                alpha =
                    ChatDesignTokens.RefinementLightIconBackgroundAlpha
            ),
        headerCancelBackground =
            colorScheme.surfaceContainerHighest.copy(
                alpha =
                    ChatDesignTokens.RefinementLightCancelBackgroundAlpha
            )
    )
}