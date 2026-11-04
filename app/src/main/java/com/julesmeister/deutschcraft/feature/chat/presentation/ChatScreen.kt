// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/ChatScreen.kt

package com.julesmeister.deutschcraft.feature.chat.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ChatBottomBar
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ChatMessageList
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ChatTopBar
import com.julesmeister.deutschcraft.feature.chat.presentation.component.MissionTopBarDrawer
import com.julesmeister.deutschcraft.feature.chat.presentation.component.missionDrawerDrag
import com.julesmeister.deutschcraft.feature.chat.presentation.component.rememberMissionDrawerState
import com.julesmeister.deutschcraft.feature.chat.presentation.component.FloatingReplySuggestions
import com.julesmeister.deutschcraft.feature.chat.presentation.component.GuestSignInPromptCard
import com.julesmeister.deutschcraft.feature.chat.presentation.component.MissionOfferCard
import com.julesmeister.deutschcraft.feature.chat.presentation.component.MissionVariationDialog
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens as ChatTokens
import com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result.MissionResultBottomSheet
import com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result.MissionResultUiState
import com.julesmeister.deutschcraft.feature.messageactions.presentation.MessageActionBottomSheet
import com.julesmeister.deutschcraft.feature.messageactions.presentation.MessageActionViewModel
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceUiState
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhrase
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhraseType
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ChatScreen(
    uiState: ChatUiState,
    missionGuidanceUiState: MissionGuidanceUiState,
    missionResultUiState: MissionResultUiState,
    markedPhrases: Map<String, List<MarkedPhrase>>,

    onAction: (ChatUiAction) -> Unit,
    onBackClick: () -> Unit,

    onOpenMissionGuidance: () -> Unit,
    onDismissMissionGuidance: () -> Unit,
    onRevealNextMissionHint: () -> Unit,
    onToggleMissionVocabulary: () -> Unit,
    onToggleMissionExamples: () -> Unit,

    onDismissMissionResult: () -> Unit,
    onContinueAfterMissionResult: () -> Unit,

    onMarkPhrase: (
        messageId: String,
        phrase: String,
        sourceSentence: String,
        type: MarkedPhraseType
    ) -> Unit,

    onRemoveMarkedPhrase: (String) -> Unit,

    onBesserImprove: (String) -> Unit =
        {},

    onBesserHint: (String) -> Unit =
        {},

    onBesserSkip: (String) -> Unit =
        {},

    modifier: Modifier =
        Modifier,

    onAddClick: () -> Unit =
        {},

    guestGate: ChatGuestGate =
        ChatGuestGate(),

    onSignInClick: () -> Unit =
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

    val glassBackdrop =
        rememberLayerBackdrop()

    val messageActionState by
    messageActionViewModel
        .uiState
        .collectAsStateWithLifecycle()

    val visibleSuggestions =
        uiState.visibleSuggestions

    var selectedMessage by
    remember {
        mutableStateOf<ChatMessage?>(
            null
        )
    }

    var scrollRequestKey by
    remember {
        mutableIntStateOf(
            0
        )
    }

    var bottomOverlayHeight by
    remember {
        mutableStateOf(
            0.dp
        )
    }

    var copyConfirmationKey by
    remember {
        mutableIntStateOf(
            0
        )
    }

    var showCopiedConfirmation by
    remember {
        mutableStateOf(
            false
        )
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

    LaunchedEffect(
        copyConfirmationKey
    ) {
        if (
            copyConfirmationKey ==
            0
        ) {
            return@LaunchedEffect
        }

        showCopiedConfirmation =
            true

        delay(
            ChatTokens
                .CopyConfirmationDurationMillis
                .milliseconds
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
                messageActionViewModel.analyzeSentence(
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

            onMarkPhrase = {
                    phrase,
                    type ->

                onMarkPhrase(
                    message.id,
                    phrase,
                    sheetSentence,
                    type
                )
            }
        )
    }

    val isMissionExpanded = missionGuidanceUiState.hasActiveMission && missionGuidanceUiState.isVisible
    val missionDrawerState = rememberMissionDrawerState()
    BackHandler(enabled = isMissionExpanded, onBack = onDismissMissionGuidance)

    MissionResultBottomSheet(
        state =
            missionResultUiState,

        onDismiss =
            onDismissMissionResult,

        onContinue =
            onContinueAfterMissionResult
    )

    MissionVariationDialog(
        state =
            uiState.missionVariation,

        onVariationSelected = { variationType ->
            onAction(
                ChatUiAction.StartMissionVariation(
                    variationType =
                        variationType
                )
            )
        },

        onDismiss = {
            onAction(
                ChatUiAction
                    .DismissMissionVariationPicker
            )
        }
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
                    modifier = Modifier.missionDrawerDrag(
                        state = missionDrawerState,
                        enabled = missionGuidanceUiState.hasActiveMission,
                        expanded = isMissionExpanded,
                        onExpand = onOpenMissionGuidance,
                        onCollapse = onDismissMissionGuidance
                    ),
                    isMissionExpanded = isMissionExpanded,
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
                        uiState.adaptiveLearningFeedbackEvent,

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
                        if (isMissionExpanded) onDismissMissionGuidance else onOpenMissionGuidance
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
                    Modifier
                        .fillMaxSize()
                        .layerBackdrop(
                            glassBackdrop
                        ),

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

                onBesserImprove =
                    onBesserImprove,

                onBesserHint =
                    onBesserHint,

                onBesserSkip =
                    onBesserSkip,

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

            val hasSuggestionContent =
                guestGate.isLocked ||
                        !uiState.isBesserRefinementMode &&
                        (
                                uiState.missionOffer !=
                                        null ||
                                        visibleSuggestions.isNotEmpty()
                                )

            ChatBottomBar(
                backdrop =
                    glassBackdrop,

                text =
                    uiState.messageInput.text,

                onTextChange = { value ->
                    onAction(
                        ChatUiAction
                            .MessageInputChanged(
                                value =
                                    TextFieldValue(
                                        text =
                                            value,

                                        selection =
                                            TextRange(
                                                value.length
                                            )
                                    )
                            )
                    )
                },

                onSend = {
                    onAction(
                        ChatUiAction.SendMessage
                    )
                },

                enabled =
                    !uiState.isInitializing &&
                            !uiState.isMissionVariationBusy &&
                            !guestGate.isLocked,

                isSending =
                    uiState.isSending ||
                            uiState
                                .isSubmittingBesserRefinement ||
                            uiState
                                .missionVariation
                                .isStarting,

                refinementInstruction =
                    uiState.besserRefinementInstruction,

                refinementFeedback =
                    uiState
                        .activeBesserRefinement
                        ?.feedback,

                isEvaluatingRefinement =
                    uiState.isSubmittingBesserRefinement,

                onCancelRefinement = {
                    onAction(
                        ChatUiAction
                            .CancelBesserRefinement
                    )
                },

                onInputFocused = {
                    scrollRequestKey +=
                        1
                },

                suggestionContent =
                    if (guestGate.isLocked) {
                        {
                            GuestSignInPromptCard(
                                onSignInClick =
                                    onSignInClick
                            )
                        }
                    } else if (hasSuggestionContent) {
                        {
                            uiState
                                .missionOffer
                                ?.let { offer ->
                                    MissionOfferCard(
                                        opportunity =
                                            offer,

                                        isStarting =
                                            uiState
                                                .isStartingMission,

                                        onStartClick = {
                                            onAction(
                                                ChatUiAction
                                                    .StartOfferedMission
                                            )
                                        },

                                        onDismissClick = {
                                            onAction(
                                                ChatUiAction
                                                    .DismissMissionOffer
                                            )
                                        }
                                    )
                                }

                            if (
                                visibleSuggestions
                                    .isNotEmpty()
                            ) {
                                FloatingReplySuggestions(
                                    suggestions =
                                        visibleSuggestions,

                                    enabled =
                                        !uiState
                                            .isInitializing &&
                                                !uiState
                                                    .isSending &&
                                                !uiState
                                                    .isMissionVariationBusy &&
                                                !uiState
                                                    .isSubmittingBesserRefinement,

                                    onSuggestionClick = { suggestionId ->
                                        onAction(
                                            ChatUiAction
                                                .SuggestionSelected(
                                                    suggestionId =
                                                        suggestionId
                                                )
                                        )

                                        scrollRequestKey +=
                                            1
                                    }
                                )
                            }
                        }
                    } else {
                        null
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

            MissionTopBarDrawer(
                state = missionDrawerState,
                guidance = missionGuidanceUiState,
                expanded = isMissionExpanded,
                onExpand = onOpenMissionGuidance,
                onCollapse = onDismissMissionGuidance,
                onRevealNextHint = onRevealNextMissionHint,
                onRetryLanguageSupport = onOpenMissionGuidance
            )
        }
    }
}

private fun ChatMessage.preferredStudySentence():
        String {

    return if (
        isShowingBesserVersion()
    ) {
        correction.orEmpty()
    } else {
        content
    }
}

private fun ChatMessage.isShowingBesserVersion():
        Boolean {

    return sender ==
            MessageSender.USER &&
            !correction.isNullOrBlank()
}
