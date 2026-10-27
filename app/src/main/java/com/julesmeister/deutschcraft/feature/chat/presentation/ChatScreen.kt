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
import com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result.MissionResultBottomSheet
import com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result.MissionResultUiState
import com.julesmeister.deutschcraft.feature.messageactions.presentation.MessageActionBottomSheet
import com.julesmeister.deutschcraft.feature.messageactions.presentation.MessageActionViewModel
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceBottomSheet
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceUiState

@Composable
fun ChatScreen(
    uiState:
    ChatUiState,

    missionGuidanceUiState:
    MissionGuidanceUiState,

    missionResultUiState:
    MissionResultUiState,

    onAction:
        (ChatUiAction) -> Unit,

    onBackClick:
        () -> Unit,

    /*
     * Active mission guidance callbacks.
     */
    onOpenMissionGuidance:
        () -> Unit,

    onDismissMissionGuidance:
        () -> Unit,

    onRevealNextMissionHint:
        () -> Unit,

    onToggleMissionVocabulary:
        () -> Unit,

    onToggleMissionExamples:
        () -> Unit,

    /*
     * Completed mission result callbacks.
     */
    onDismissMissionResult:
        () -> Unit,

    onContinueAfterMissionResult:
        () -> Unit,

    modifier:
    Modifier =
        Modifier,

    onAddClick:
        () -> Unit =
        {},

    messageActionViewModel:
    MessageActionViewModel =
        hiltViewModel()
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
     * Message selected for:
     *
     * - translation
     * - explanation
     * - rephrasing
     */
    var selectedMessage by
    remember {
        mutableStateOf<ChatMessage?>(
            null
        )
    }

    /*
     * Used to keep the newest message visible when the
     * keyboard opens.
     */
    var scrollRequestKey by
    remember {
        mutableIntStateOf(
            0
        )
    }

    /*
     * Measured height of the floating bottom controls.
     */
    var bottomOverlayHeight by
    remember {
        mutableStateOf(
            0.dp
        )
    }

    /*
     * Show normal chat errors through Snackbar, then consume
     * the event.
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
     * =======================================================
     * MESSAGE ACTION SHEET
     * =======================================================
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

    /*
     * =======================================================
     * MISSION GUIDANCE SHEET
     * =======================================================
     *
     * Only appears when:
     *
     * - an active objective exists
     * - the learner taps the mission area in the top bar
     */
    MissionGuidanceBottomSheet(
        state =
            missionGuidanceUiState,

        onDismiss =
            onDismissMissionGuidance,

        onRevealNextHint =
            onRevealNextMissionHint,

        onToggleVocabulary =
            onToggleMissionVocabulary,

        onToggleExamples =
            onToggleMissionExamples
    )

    /*
     * =======================================================
     * MISSION RESULT SHEET
     * =======================================================
     *
     * Appears after final mission analytics have been
     * calculated and persisted.
     */
    MissionResultBottomSheet(
        state =
            missionResultUiState,

        onDismiss =
            onDismissMissionResult,

        onContinue =
            onContinueAfterMissionResult
    )

    Scaffold(
        modifier =
            modifier.fillMaxSize(),

        containerColor =
            MaterialTheme
                .colorScheme
                .background,

        contentWindowInsets =
            WindowInsets(
                left =
                    0,
                top =
                    0,
                right =
                    0,
                bottom =
                    0
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

                adaptiveLearningFeedbackEvent =
                    uiState
                        .adaptiveLearningFeedbackEvent,

                onObjectiveCompletionShown = { eventId ->
                    onAction(
                        ChatUiAction
                            .ObjectiveCompletionShown(
                                eventId =
                                    eventId
                            )
                    )
                },

                onAdaptiveLearningFeedbackShown = { eventId ->
                    onAction(
                        ChatUiAction
                            .AdaptiveLearningFeedbackShown(
                                eventId =
                                    eventId
                            )
                    )
                },

                /*
                 * The click reaches MissionTopBarPager through:
                 *
                 * ChatTopBar
                 *     ↓
                 * AnimatedChatTopBarTitle
                 *     ↓
                 * MissionTopBarPager
                 */
                onMissionGuidanceClick =
                    onOpenMissionGuidance
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
            modifier =
                Modifier
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
                     * Clear previous analysis before opening
                     * another message.
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
                    scrollRequestKey +=
                        1
                },

                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .onSizeChanged { size ->
                            val measuredHeight =
                                with(
                                    density
                                ) {
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