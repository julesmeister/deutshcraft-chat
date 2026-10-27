package com.julesmeister.deutschcraft.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/*
 * Must be declared before DeutschCraftLightColorScheme because
 * the color scheme references it during top-level
 * initialization.
 */
private val ColorLightError =
    Color(
        0xFFBA1A1A
    )

private val DeutschCraftDarkColorScheme =
    darkColorScheme(
        primary =
            DeutschLime,
        onPrimary =
            DeutschTextOnLime,

        primaryContainer =
            DeutschLimeMuted,
        onPrimaryContainer =
            DeutschTextPrimary,

        secondary =
            DeutschLimeSoft,
        onSecondary =
            DeutschTextOnLime,

        secondaryContainer =
            DeutschSurfaceHighest,
        onSecondaryContainer =
            DeutschTextPrimary,

        tertiary =
            DeutschInfo,
        onTertiary =
            DeutschBlack,

        background =
            DeutschBlack,
        onBackground =
            DeutschTextPrimary,

        surface =
            DeutschBlackSoft,
        onSurface =
            DeutschTextPrimary,

        surfaceVariant =
            DeutschSurfaceHigh,
        onSurfaceVariant =
            DeutschTextSecondary,

        surfaceContainer =
            DeutschSurface,
        surfaceContainerLow =
            DeutschBlackSoft,
        surfaceContainerHigh =
            DeutschSurfaceHigh,
        surfaceContainerHighest =
            DeutschSurfaceHighest,

        outline =
            DeutschOutline,
        outlineVariant =
            DeutschOutlineSoft,

        error =
            DeutschError,
        onError =
            DeutschBlack
    )

private val DeutschCraftLightColorScheme =
    lightColorScheme(
        primary =
            DeutschLimeDark,
        onPrimary =
            DeutschTextOnLime,

        primaryContainer =
            DeutschLightPrimaryContainer,
        onPrimaryContainer =
            DeutschLightTextPrimary,

        secondary =
            DeutschLimeDark,
        onSecondary =
            DeutschTextOnLime,

        secondaryContainer =
            DeutschLightAssistantContainer,
        onSecondaryContainer =
            DeutschLightTextPrimary,

        tertiary =
            DeutschInfo,
        onTertiary =
            DeutschTextOnSky,

        background =
            DeutschLightBackground,
        onBackground =
            DeutschLightTextPrimary,

        surface =
            DeutschLightBackgroundSoft,
        onSurface =
            DeutschLightTextPrimary,

        surfaceVariant =
            DeutschLightSurfaceHigh,
        onSurfaceVariant =
            DeutschLightTextSecondary,

        surfaceContainer =
            DeutschLightSurface,
        surfaceContainerLow =
            DeutschLightBackgroundSoft,
        surfaceContainerHigh =
            DeutschLightSurfaceHigh,
        surfaceContainerHighest =
            DeutschLightSurfaceHighest,

        outline =
            DeutschLightOutline,
        outlineVariant =
            DeutschLightOutlineSoft,

        error =
            ColorLightError,
        onError =
            DeutschLightSurface
    )

@Composable
fun DeutschCraftTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (darkTheme) {
            DeutschCraftDarkColorScheme
        } else {
            DeutschCraftLightColorScheme
        }

    val view =
        LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window =
                (view.context as Activity).window

            window.statusBarColor =
                colorScheme.background.toArgb()

            window.navigationBarColor =
                colorScheme.background.toArgb()

            val insetsController =
                WindowCompat.getInsetsController(
                    window,
                    view
                )

            /*
             * Light background requires dark Android status-bar
             * icons.
             */
            insetsController.isAppearanceLightStatusBars =
                !darkTheme

            insetsController.isAppearanceLightNavigationBars =
                !darkTheme

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
            ) {
                window.isNavigationBarContrastEnforced =
                    false
            }
        }
    }

    MaterialTheme(
        colorScheme =
            colorScheme,
        typography =
            Typography,
        content =
            content
    )
}