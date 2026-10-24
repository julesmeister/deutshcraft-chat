package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ChatBottomBar
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ChatMessageList
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ChatTopBar

@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onAction: (ChatUiAction) -> Unit,
    onBackClick: () -> Unit,
    onProgressClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNewConversationClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onClearConversationClick: () -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(uiState.errorMessage) {
        val errorMessage =
            uiState.errorMessage
                ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(
            message = errorMessage
        )

        onAction(
            ChatUiAction.ErrorDismissed
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor =
            MaterialTheme.colorScheme.background,
        topBar = {
            ChatTopBar(
                isAssistantTyping =
                    uiState.isAssistantTyping,
                isAssessing =
                    uiState.isAssessing,
                onBackClick =
                    onBackClick,
                onProgressClick =
                    onProgressClick,
                onNewConversationClick =
                    onNewConversationClick,
                onSettingsClick =
                    onSettingsClick,
                onClearConversationClick =
                    onClearConversationClick
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        bottomBar = {
            ChatBottomBar(
                uiState = uiState,
                onAction = onAction,
                onAddClick = onAddClick
            )
        }
    ) { innerPadding ->
        ChatMessageList(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background
                )
                .padding(innerPadding),
            uiState = uiState
        )
    }
}