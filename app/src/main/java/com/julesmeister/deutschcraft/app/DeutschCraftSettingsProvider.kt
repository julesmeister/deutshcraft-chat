package com.julesmeister.deutschcraft.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.util.copy
import com.julesmeister.deutschcraft.feature.settings.presentation.SettingsViewModel

@Composable
fun DeutschCraftSettingsProvider(
    content: @Composable () -> Unit
) {
    val viewModel:
            SettingsViewModel =
        hiltViewModel()

    val settings by
    viewModel
        .settings
        .collectAsStateWithLifecycle()

    val currentDensity =
        LocalDensity.current

    val scaledDensity =
        currentDensity.copy(
            fontScale =
                currentDensity.fontScale *
                        settings.fontScale
        )

    CompositionLocalProvider(
        LocalDensity provides
                scaledDensity
    ) {
        content()
    }
}