package com.julesmeister.deutschcraft.feature.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsRoute(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel =
        hiltViewModel()
) {
    val settings by
    viewModel
        .settings
        .collectAsStateWithLifecycle()

    SettingsScreen(
        settings =
            settings,
        onAction =
            viewModel::onAction,
        onBackClick =
            onBackClick
    )
}