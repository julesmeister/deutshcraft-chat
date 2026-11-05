package com.julesmeister.deutschcraft.feature.settings.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun SettingsRoute(
    onBackClick: () -> Unit,
    onSignInClick: () -> Unit,
    viewModel: SettingsViewModel =
        hiltViewModel()
) {
    val settings by
    viewModel
        .settings
        .collectAsStateWithLifecycle()

    val session by
    viewModel
        .session
        .collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.snackbarEvents.collect { message ->
                snackbarHostState.showSnackbar(
                    message = message,
                    withDismissAction = true
                )
            }
        }
    }

    SettingsScreen(
        settings =
            settings,
        onAction =
            viewModel::onAction,
        onBackClick =
            onBackClick,
        session =
            session,
        onSignInClick =
            onSignInClick,
        snackbarHostState =
            snackbarHostState
    )
}