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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ChatBottomBar
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ChatMessageList
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ChatTopBar
import com.julesmeister.deutschcraft.feature.messageactions.presentation.MessageActionBottomSheet
import com.julesmeister.deutschcraft.feature.messageactions.presentation.MessageActionViewModel

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
    onAddClick: () -> Unit = {},
    messageActionViewModel:
    MessageActionViewModel = hiltViewModel()
) {
    val snackbarHostState =
        remember {
            SnackbarHostState()
        }

    val messageActionState by
    messageActionViewModel
        .uiState
        .collectAsStateWithLifecycle()

    var selectedMessage by remember {
        mutableStateOf<ChatMessage?>(null)
    }

    var scrollRequestKey by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(
        uiState.errorMessage
    ) {
        val errorMessage =
            uiState.errorMessage
                ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(
            message =
                errorMessage
        )

        onAction(
            ChatUiAction.ErrorDismissed
        )
    }

    selectedMessage?.let { message ->
        MessageActionBottomSheet(
            sentence =
                message.content,
            selectedAction =
                messageActionState.selectedAction,
            insight =
                messageActionState.insight,
            isLoading =
                messageActionState.isLoading,
            errorMessage =
                messageActionState.errorMessage,
            onActionClick = { action ->
                messageActionViewModel
                    .analyzeSentence(
                        sentence =
                            message.content,
                        action =
                            action
                    )
            },
            onDismiss = {
                selectedMessage = null
                messageActionViewModel.reset()
            }
        )
    }

    Scaffold(
        modifier =
            modifier.fillMaxSize(),
        containerColor =
            MaterialTheme
                .colorScheme
                .background,
        topBar = {
            ChatTopBar(
                isAssistantTyping =
                    uiState.isAssistantTyping,
                isAssessing =
                    uiState.isAssessing,
                streakDays =
                    uiState.currentStreak,
                activeMissionTitle =
                    uiState.activeMissionTitle,
                currentMissionObjective =
                    uiState.currentMissionObjective,
                missionProgressPercentage =
                    uiState.missionProgressPercentage,
                objectiveCompletionEvent =
                    uiState.objectiveCompletionEvent,
                onObjectiveCompletionShown = { eventId ->
                    onAction(
                        ChatUiAction
                            .ObjectiveCompletionShown(
                                eventId = eventId
                            )
                    )
                },
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
                hostState =
                    snackbarHostState
            )
        },
        bottomBar = {
            ChatBottomBar(
                uiState =
                    uiState,
                onAction =
                    onAction,
                onAddClick =
                    onAddClick,
                onInputFocused = {
                    scrollRequestKey += 1
                }
            )
        }
    ) { innerPadding ->
        ChatMessageList(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme
                        .colorScheme
                        .background
                )
                .padding(
                    innerPadding
                ),
            uiState =
                uiState,
            scrollRequestKey =
                scrollRequestKey,
            onMessageClick = { message ->
                messageActionViewModel.reset()
                selectedMessage = message
            }
        )
    }
}