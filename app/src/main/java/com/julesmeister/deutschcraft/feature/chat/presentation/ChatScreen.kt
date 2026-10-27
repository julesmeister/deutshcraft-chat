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
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ChatBottomBar
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ChatMessageList
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ChatTopBar
import com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result.MissionResultBottomSheet
import com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result.MissionResultUiState
import com.julesmeister.deutschcraft.feature.messageactions.presentation.MessageActionBottomSheet
import com.julesmeister.deutschcraft.feature.messageactions.presentation.MessageActionViewModel
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceBottomSheet
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceUiState
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhrase
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhraseType
import kotlinx.coroutines.delay

@Composable
fun ChatScreen(
    uiState:
    ChatUiState,

    missionGuidanceUiState:
    MissionGuidanceUiState,

    missionResultUiState:
    MissionResultUiState,

    markedPhrases:
    Map<String, List<MarkedPhrase>>,

    onAction:
        (ChatUiAction) -> Unit,

    onBackClick:
        () -> Unit,

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

    onDismissMissionResult:
        () -> Unit,

    onContinueAfterMissionResult:
        () -> Unit,

    onMarkPhrase:
        (
        messageId: String,
        phrase: String,
        sourceSentence: String,
        type: MarkedPhraseType
    ) -> Unit,

    onRemoveMarkedPhrase:
        (String) -> Unit,

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

    var selectedMessage by
    remember {
        mutableStateOf<ChatMessage?>(
            null
        )
    }

    var scrollRequestKey by
    remember {
        mutableIntStateOf(0)
    }

    var bottomOverlayHeight by
    remember {
        mutableStateOf(0.dp)
    }

    /*
     * Every copy increments this key.
     *
     * Repeated copies restart the confirmation timer.
     */
    var copyConfirmationKey by
    remember {
        mutableIntStateOf(0)
    }

    var showCopiedConfirmation by
    remember {
        mutableStateOf(false)
    }

    /*
     * Keep Snackbar only for real chat errors.
     *
     * Clipboard confirmation never uses this host.
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
     * Brief top-bar confirmation.
     */
    LaunchedEffect(
        copyConfirmationKey
    ) {
        if (copyConfirmationKey == 0) {
            return@LaunchedEffect
        }

        showCopiedConfirmation =
            true

        delay(
            COPY_CONFIRMATION_DURATION_MILLIS
        )

        showCopiedConfirmation =
            false
    }

    selectedMessage?.let { message ->
        val sheetSentence =
            message.preferredStudySentence()

        MessageActionBottomSheet(
            sentence =
                sheetSentence,

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
                            sheetSentence,
                        action =
                            action
                    )
            },

            onDismiss = {
                selectedMessage =
                    null

                messageActionViewModel.reset()
            },

            isCorrectedVersion =
                message.isShowingBesserVersion(),

            markedPhrases =
                markedPhrases[
                    message.id
                ].orEmpty(),

            onMarkPhrase = { phrase, type ->
                onMarkPhrase(
                    message.id,
                    phrase,
                    sheetSentence,
                    type
                )
            }
        )
    }

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

                adaptiveLearningFeedbackEvent =
                    uiState
                        .adaptiveLearningFeedbackEvent,

                /*
                 * This is the actual connection from the
                 * long-press copy event to the top bar.
                 */
                showCopiedConfirmation =
                    showCopiedConfirmation,

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

                markedPhrases =
                    markedPhrases,

                onRemoveMarkedPhrase =
                    onRemoveMarkedPhrase,

                /*
                 * MessageBubble has already:
                 *
                 * 1. Copied the visible primary sentence.
                 * 2. Started the animated circle beside the
                 *    bubble.
                 *
                 * This starts the separate top-bar message.
                 */
                onMessageCopied = {
                    copyConfirmationKey +=
                        1
                },

                onMessageClick = { message ->
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

private fun ChatMessage.preferredStudySentence(): String {
    return if (isShowingBesserVersion()) {
        correction.orEmpty()
    } else {
        content
    }
}

private fun ChatMessage.isShowingBesserVersion(): Boolean {
    return sender ==
            MessageSender.USER &&
            !correction.isNullOrBlank()
}

private const val COPY_CONFIRMATION_DURATION_MILLIS =
    1_100L