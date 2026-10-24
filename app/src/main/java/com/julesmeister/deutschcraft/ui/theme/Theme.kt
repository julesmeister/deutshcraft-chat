package com.julesmeister.deutschcraft.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DeutschCraftDarkColorScheme =
    darkColorScheme(
        primary = DeutschLime,
        onPrimary = DeutschTextOnLime,

        primaryContainer = DeutschLimeMuted,
        onPrimaryContainer = DeutschTextPrimary,

        secondary = DeutschLimeSoft,
        onSecondary = DeutschTextOnLime,

        secondaryContainer = DeutschSurfaceHighest,
        onSecondaryContainer = DeutschTextPrimary,

        tertiary = DeutschInfo,
        onTertiary = DeutschBlack,

        background = DeutschBlack,
        onBackground = DeutschTextPrimary,

        surface = DeutschBlackSoft,
        onSurface = DeutschTextPrimary,

        surfaceVariant = DeutschSurfaceHigh,
        onSurfaceVariant = DeutschTextSecondary,

        surfaceContainer = DeutschSurface,
        surfaceContainerLow = DeutschBlackSoft,
        surfaceContainerHigh = DeutschSurfaceHigh,
        surfaceContainerHighest = DeutschSurfaceHighest,

        outline = DeutschOutline,
        outlineVariant = DeutschOutlineSoft,

        error = DeutschError,
        onError = DeutschBlack
    )

@Composable
fun DeutschCraftTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DeutschCraftDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window =
                (view.context as Activity).window

            window.statusBarColor =
                DeutschBlack.toArgb()

            window.navigationBarColor =
                DeutschBlack.toArgb()

            WindowCompat
                .getInsetsController(
                    window,
                    view
                )
                .isAppearanceLightStatusBars = false

            WindowCompat
                .getInsetsController(
                    window,
                    view
                )
                .isAppearanceLightNavigationBars = false

            if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
            ) {
                window.isNavigationBarContrastEnforced =
                    false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
