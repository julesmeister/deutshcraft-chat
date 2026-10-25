package com.julesmeister.deutschcraft.app.navigati

import androidx.compose.runtime.LaunchedEffect
import com.julesmeister.deutschcraft.app.navigation.StartupNavigationState
import com.julesmeister.deutschcraft.app.navigation.StartupViewModel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.deutschcraft.ui.theme.DeutschLime

@Composable
fun StartupRoute(
    onOpenChat: (String) -> Unit,
    onOpenThreadList: () -> Unit,
    viewModel: StartupViewModel =
        hiltViewModel()
) {
    val navigationState by viewModel
        .navigationState
        .collectAsStateWithLifecycle()

    LaunchedEffect(navigationState) {
        when (
            val state = navigationState
        ) {
            StartupNavigationState.Loading -> {
                Unit
            }

            is StartupNavigationState.OpenChat -> {
                onOpenChat(
                    state.conversationId
                )
            }

            StartupNavigationState.OpenThreadList -> {
                onOpenThreadList()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = DeutschLime
        )
    }
}