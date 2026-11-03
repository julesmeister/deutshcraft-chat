package com.julesmeister.deutschcraft.feature.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.julesmeister.deutschcraft.feature.settings.domain.UserSettings

val LocalUserSettings = staticCompositionLocalOf { UserSettings() }

@Composable
fun AppSettingsProvider(
    settings: UserSettings,
    content: @Composable () -> Unit
) {
    val systemDensity = LocalDensity.current
    val appDensity = remember(systemDensity, settings.fontScale) {
        systemDensity.withAppFontScale(settings.fontScale)
    }

    CompositionLocalProvider(
        LocalUserSettings provides settings,
        LocalDensity provides appDensity,
        content = content
    )
}

internal fun Density.withAppFontScale(appFontScale: Float): Density =
    Density(density = density, fontScale = fontScale * appFontScale)
