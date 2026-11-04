package com.julesmeister.deutschcraft.feature.auth.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/*
 * Accent for text, icons and focus borders: lime in dark mode,
 * a readable olive in light mode.
 */
internal val authAccent: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val colorScheme = MaterialTheme.colorScheme

        return if (
            colorScheme.background.luminance() < AuthDesignTokens.DarkLuminanceThreshold
        ) {
            colorScheme.primary
        } else {
            AuthDesignTokens.LightAccentText
        }
    }
