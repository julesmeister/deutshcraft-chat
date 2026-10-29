package com.julesmeister.deutschcraft.feature.chat.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.TextUnit

/*
 * Chat-specific typography scaling.
 *
 * SettingsRepository remains the persisted source of truth.
 * ChatRoute provides the current setting once around the whole
 * chat presentation tree.
 *
 * Child components can read LocalChatFontScale without passing
 * a fontScale parameter through every intermediate composable.
 */
val LocalChatFontScale =
    staticCompositionLocalOf {
        DEFAULT_CHAT_FONT_SCALE
    }

/*
 * Applies DeutschCraft's app-level chat font scaling to a
 * Compose TextUnit.
 *
 * Compose still applies the device accessibility font scale
 * because the resulting value remains an sp TextUnit.
 */
fun TextUnit.scaledForChat(
    scale: Float
): TextUnit {
    return this *
            scale.coerceIn(
                minimumValue =
                    MINIMUM_CHAT_FONT_SCALE,
                maximumValue =
                    MAXIMUM_CHAT_FONT_SCALE
            )
}

private const val DEFAULT_CHAT_FONT_SCALE =
    1f

private const val MINIMUM_CHAT_FONT_SCALE =
    0.75f

private const val MAXIMUM_CHAT_FONT_SCALE =
    1.50f