package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ChatRoute(
    onBackClick: () -> Unit,
    viewModel: ChatViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    ChatScreen(
        uiState =
            uiState,
        onAction =
            viewModel::onAction,
        onBackClick =
            onBackClick
    )
}