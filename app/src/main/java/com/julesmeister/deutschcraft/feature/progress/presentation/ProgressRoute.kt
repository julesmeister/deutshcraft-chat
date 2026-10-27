package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.deutschcraft.feature.settings.presentation.SettingsUiAction
import com.julesmeister.deutschcraft.feature.settings.presentation.SettingsViewModel

@Composable
fun ProgressRoute(
    onBackClick: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    val settings by
    settingsViewModel
        .settings
        .collectAsStateWithLifecycle()

    ProgressScreen(
        uiState =
            uiState,

        progressLanguage =
            settings.progressLanguage,

        onProgressLanguageChanged = { language ->
            settingsViewModel.onAction(
                SettingsUiAction.ProgressLanguageChanged(
                    language =
                        language
                )
            )
        },

        onBackClick =
            onBackClick
    )
}