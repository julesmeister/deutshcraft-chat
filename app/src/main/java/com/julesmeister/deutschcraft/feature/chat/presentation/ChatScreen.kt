package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
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
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = {},
    messageActionViewModel:
    MessageActionViewModel = hiltViewModel()
) {
    val snackbarHostState =
        remember {
            SnackbarHostState()
        }

    val density =
        LocalDensity.current

    val messageActionState by
    messageActionViewModel
        .uiState
        .collectAsStateWithLifecycle()

    /*
     * The message selected for sentence-level learning
     * actions such as translation, explanation, and
     * rephrasing.
     */
    var selectedMessage by
    remember {
        mutableStateOf<ChatMessage?>(
            null
        )
    }

    /*
     * Incremented when the composer receives focus.
     *
     * ChatMessageList uses this key to keep the newest
     * message visible when the keyboard opens.
     */
    var scrollRequestKey by
    remember {
        mutableIntStateOf(0)
    }

    /*
     * Actual measured height of the floating mission offer,
     * reply suggestions, composer, and navigation-bar
     * padding.
     *
     * The message list reserves this exact amount of bottom
     * scrolling space so the newest message remains visible
     * above the floating controls.
     */
    var bottomOverlayHeight by
    remember {
        mutableStateOf(
            0.dp
        )
    }

    /*
     * Show chat errors through the screen's Snackbar host,
     * then consume the error event.
     */
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

    /*
     * Display the sentence-action sheet only while a message
     * is selected.
     */
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
                selectedMessage =
                    null

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
        contentWindowInsets =
            WindowInsets(
                left = 0,
                top = 0,
                right = 0,
                bottom = 0
            ),
        topBar = {
            ChatTopBar(
                isAssistantTyping =
                    uiState.isAssistantTyping,
                isAssessing =
                    uiState.isAssessing,
                onBackClick =
                    onBackClick,
                activeMissionTitle =
                    uiState.activeMissionTitle,
                currentMissionObjective =
                    uiState.currentMissionObjective,
                currentMissionGuidance =
                    uiState.currentMissionGuidance,
                missionProgressPercentage =
                    uiState.missionProgressPercentage,
                objectiveCompletionEvent =
                    uiState.objectiveCompletionEvent,
                onObjectiveCompletionShown = { eventId ->
                    onAction(
                        ChatUiAction
                            .ObjectiveCompletionShown(
                                eventId =
                                    eventId
                            )
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState =
                    snackbarHostState
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color =
                        MaterialTheme
                            .colorScheme
                            .background
                )
                .padding(
                    top =
                        innerPadding
                            .calculateTopPadding()
                )
        ) {
            ChatMessageList(
                modifier =
                    Modifier.fillMaxSize(),
                uiState =
                    uiState,
                scrollRequestKey =
                    scrollRequestKey,
                bottomOverlayHeight =
                    bottomOverlayHeight,
                onMessageClick = { message ->
                    /*
                     * Clear previously generated analysis
                     * before opening the newly selected
                     * message.
                     */
                    messageActionViewModel.reset()

                    selectedMessage =
                        message
                }
            )

            ChatBottomBar(
                uiState =
                    uiState,
                onAction =
                    onAction,
                onAddClick =
                    onAddClick,
                onInputFocused = {
                    /*
                     * Request that the message list keep the
                     * latest message visible above the opened
                     * keyboard.
                     */
                    scrollRequestKey +=
                        1
                },
                modifier = Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .onSizeChanged { size ->
                        val measuredHeight =
                            with(density) {
                                size.height.toDp()
                            }

                        if (
                            bottomOverlayHeight !=
                            measuredHeight
                        ) {
                            bottomOverlayHeight =
                                measuredHeight
                        }
                    }
            )
        }
    }
}