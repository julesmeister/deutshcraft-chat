// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/ChatSessionCoordinator.kt

package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.besser.presentation.BesserChallengeUiModel
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestionAction
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result.MissionResultController
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationContext
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationMode
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationOption
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationType
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationUiState
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatSessionCoordinator
@Inject constructor(
    private val sessionInitializer:
    ChatSessionInitializer,

    private val sessionObservers:
    ChatSessionObservers,

    private val messageController:
    ChatSessionMessageController,

    private val missionController:
    ChatSessionMissionController,

    private val missionResultController:
    MissionResultController
) {

    private val _uiState =
        MutableStateFlow(
            ChatUiState()
        )

    val uiState: StateFlow<ChatUiState> =
        _uiState.asStateFlow()

    private val runtime =
        ChatSessionRuntime()

    private var provisionalGreetingMessageId: String? =
        null

    fun start(
        conversationId: String,
        scope: CoroutineScope
    ) {
        if (
            runtime.isInitializing ||
            runtime.isInitialized
        ) {
            return
        }

        sessionObservers.observeStreak(
            scope =
                scope,

            onStreakChanged =
                ::applyStreakUpdate
        )

        runtime.initializationJob =
            scope.launch {
                initialize(
                    requestedConversationId =
                        conversationId,

                    scope =
                        scope
                )
            }
    }

    fun onAction(
        action: ChatUiAction,
        scope: CoroutineScope
    ) {
        when (action) {
            is ChatUiAction.MessageInputChanged -> {
                messageController.updateInput(
                    value =
                        action.value,

                    uiState =
                        _uiState
                )
            }

            is ChatUiAction.SuggestionSelected -> {
                handleSuggestionSelection(
                    suggestionId =
                        action.suggestionId
                )
            }

            ChatUiAction.OpenMissionVariationPicker -> {
                openMissionVariationPicker()
            }

            ChatUiAction.DismissMissionVariationPicker -> {
                _uiState.update { state ->
                    state.missionVariationPickerDismissed()
                }
            }

            ChatUiAction.DismissMissionContinuation -> {
                dismissMissionContinuation()
            }

            is ChatUiAction.StartMissionVariation -> {
                missionController.startMissionVariation(
                    variationType =
                        action.variationType,

                    scope =
                        scope,

                    runtime =
                        runtime,

                    uiState =
                        _uiState
                )
            }

            ChatUiAction.SendMessage -> {
                messageController.send(
                    scope =
                        scope,

                    runtime =
                        runtime,

                    uiState =
                        _uiState,

                    onMissionOpportunityCheckRequested =
                        ::checkForMissionOpportunity,

                    onMissionCompleted =
                        ::showMissionResult,

                    onError =
                        ::showError
                )
            }

            ChatUiAction.CancelBesserRefinement -> {
                cancelBesserRefinement()
            }

            ChatUiAction.StartOfferedMission -> {
                missionController.startOfferedMission(
                    scope =
                        scope,

                    runtime =
                        runtime,

                    uiState =
                        _uiState
                )
            }

            ChatUiAction.DismissMissionOffer -> {
                missionController.dismissOffer(
                    runtime =
                        runtime,

                    uiState =
                        _uiState
                )
            }

            is ChatUiAction.ObjectiveCompletionShown -> {
                missionController.consumeObjectiveCompletion(
                    eventId =
                        action.eventId,

                    uiState =
                        _uiState
                )
            }

            is ChatUiAction.AdaptiveLearningFeedbackShown -> {
                consumeAdaptiveLearningFeedback(
                    eventId =
                        action.eventId
                )
            }

            ChatUiAction.ErrorDismissed -> {
                dismissError()
            }
        }
    }

    fun showMissionContinuation(
        context: MissionVariationContext
    ) {
        runtime.cancelSuggestionJob()

        runtime
            .activeConversationId
            ?.let { conversationId ->
                messageController.clearSuggestions(
                    conversationId =
                        conversationId
                )
            }

        _uiState.update { state ->
            state.missionContinuationAvailable(
                context =
                    context
            )
        }
    }

    private fun handleSuggestionSelection(
        suggestionId: String
    ) {
        if (
            suggestionId ==
            ChatPresentationTokens.UiState.ActiveMissionBranchSuggestionId
        ) {
            openActiveMissionVariationPicker()

            return
        }

        val suggestion =
            _uiState
                .value
                .suggestions
                .firstOrNull { candidate ->
                    candidate.id ==
                            suggestionId
                }
                ?: return

        when (suggestion.action) {
            ReplySuggestionAction.FillComposer -> {
                messageController.selectSuggestion(
                    suggestionId =
                        suggestionId,

                    uiState =
                        _uiState
                )
            }

            ReplySuggestionAction.OpenMissionVariations -> {
                openMissionVariationPicker()
            }

            ReplySuggestionAction.DismissMissionContinuation -> {
                dismissMissionContinuation()
            }
        }
    }

    private fun openMissionVariationPicker() {
        val variationContext =
            _uiState
                .value
                .missionVariation
                .context

        if (variationContext != null) {
            _uiState.update { state ->
                state.missionVariationPickerOpened()
            }

            return
        }

        openActiveMissionVariationPicker()
    }

    private fun openActiveMissionVariationPicker() {
        val activeMission =
            runtime.activeMission
                ?: run {
                    showError(
                        message =
                            ChatPresentationTokens.SessionCoordinator.ActiveMissionUnavailableError
                    )

                    return
                }

        val currentState =
            _uiState.value

        if (
            currentState.isInitializing ||
            currentState.isSending ||
            currentState.isStartingMission ||
            currentState.isSubmittingBesserRefinement ||
            currentState.missionVariation.isStarting
        ) {
            return
        }

        val context =
            activeMission.toVariationContext()

        _uiState.update { state ->
            state.copy(
                missionVariation =
                    MissionVariationUiState(
                        context =
                            context,

                        options =
                            createActiveMissionVariationOptions(),

                        isPickerVisible =
                            true,

                        isStarting =
                            false
                    ),

                errorMessage =
                    null
            )
        }

        Log.d(
            ChatPresentationTokens.SessionCoordinator.CoordinatorTag,
            buildString {
                append(
                    "Active mission variation picker opened. "
                )

                append(
                    "missionId="
                )

                append(
                    activeMission.id
                )

                append(
                    ", rootTemplateId="
                )

                append(
                    activeMission
                        .resolvedRootMissionTemplateId
                )

                append(
                    ", currentTemplateId="
                )

                append(
                    activeMission.template.id
                )
            }
        )
    }

    private fun ConversationMission.toVariationContext():
            MissionVariationContext {

        return MissionVariationContext(
            mode =
                MissionVariationMode
                    .BRANCH_ACTIVE_MISSION,

            sourceMissionId =
                id,

            missionTitle =
                template
                    .title
                    .trim()
                    .ifBlank {
                        ChatPresentationTokens.SessionCoordinator.DefaultMissionTitle
                    },

            cefrLevel =
                template
                    .difficulty
                    .name
                    .toCefrLevel(),

            overallScore =
                0,

            grammarScore =
                0,

            vocabularyScore =
                0,

            strongObjectiveTitles =
                objectiveProgress
                    .filter { progress ->
                        progress.isCompleted
                    }
                    .map { progress ->
                        progress.objective.title
                    }
                    .sanitizedValues(),

            improvementObjectiveTitles =
                objectiveProgress
                    .filterNot { progress ->
                        progress.isCompleted
                    }
                    .map { progress ->
                        progress.objective.title
                    }
                    .sanitizedValues(),

            demonstratedVocabulary =
                emptyList(),

            unusedTargetVocabulary =
                objectiveProgress
                    .filterNot { progress ->
                        progress.isCompleted
                    }
                    .flatMap { progress ->
                        progress
                            .objective
                            .targetVocabulary
                    }
                    .sanitizedValues()
        )
    }

    private fun createActiveMissionVariationOptions():
            List<MissionVariationOption> {

        return listOf(
            MissionVariationOption(
                type =
                    MissionVariationType
                        .COMPLICATION,

                title =
                    "Neue Situation",

                description =
                    "Entdecke eine andere, noch nicht besprochene " +
                            "Seite derselben Mission."
            ),

            MissionVariationOption(
                type =
                    MissionVariationType
                        .NEW_VOCABULARY,

                title =
                    "Neue Wörter",

                description =
                    "Führe die Mission in eine Richtung mit neuem, " +
                            "passendem Wortschatz."
            ),

            MissionVariationOption(
                type =
                    MissionVariationType
                        .GRAMMAR_STRETCH,

                title =
                    "Grammatik-Stufe",

                description =
                    "Vertiefe dieselbe Mission mit natürlicheren " +
                            "und anspruchsvolleren Strukturen."
            )
        )
    }

    private fun String.toCefrLevel():
            CefrLevel {

        return runCatching {
            CefrLevel.valueOf(
                trim().uppercase()
            )
        }
            .getOrDefault(
                CefrLevel.A1
            )
    }

    private fun List<String>.sanitizedValues():
            List<String> {

        return asSequence()
            .map(
                String::trim
            )
            .filter(
                String::isNotBlank
            )
            .distinct()
            .toList()
    }

    private fun dismissMissionContinuation() {
        runtime
            .activeConversationId
            ?.let { conversationId ->
                messageController.clearSuggestions(
                    conversationId =
                        conversationId
                )
            }

        _uiState.update { state ->
            state.missionContinuationDismissed()
        }
    }

    fun stop() {
        provisionalGreetingMessageId =
            null

        runtime.clear()

        sessionObservers.stop()
    }

    private suspend fun initialize(
        requestedConversationId: String,
        scope: CoroutineScope
    ) {
        try {
            val initializationResult =
                sessionInitializer.initialize(
                    requestedConversationId =
                        requestedConversationId
                )

            runtime.activeConversationId =
                initializationResult.conversationId

            provisionalGreetingMessageId =
                initializationResult
                    .provisionalMessages
                    .firstOrNull()
                    ?.id

            val cachedMissionOffer =
                missionController.restoreOffer(
                    conversationId =
                        initializationResult.conversationId
                )

            _uiState.update { state ->
                state.copy(
                    messages =
                        initializationResult
                            .provisionalMessages,

                    suggestions =
                        initializationResult
                            .cachedSuggestions,

                    missionOffer =
                        cachedMissionOffer
                )
            }

            observeConversation(
                conversationId =
                    initializationResult.conversationId,

                scope =
                    scope
            )

            _uiState.update { state ->
                state.initializationSucceeded()
            }
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                ChatPresentationTokens.SessionCoordinator.CoordinatorTag,
                "Chat initialization failed.",
                throwable
            )

            provisionalGreetingMessageId =
                null

            _uiState.update { state ->
                state.initializationFailed(
                    message =
                        throwable.message
                            ?: ChatPresentationTokens.SessionCoordinator.LoadError
                )
            }
        } finally {
            runtime.initializationJob =
                null
        }
    }

    private fun observeConversation(
        conversationId: String,
        scope: CoroutineScope
    ) {
        sessionObservers.observeConversation(
            conversationId =
                conversationId,

            scope =
                scope,

            onMessagesChanged =
                ::applyMessageUpdate,

            onMissionChanged = { update ->
                missionController.applyMissionUpdate(
                    update =
                        update,

                    runtime =
                        runtime,

                    uiState =
                        _uiState
                )
            },

            onBesserChallengesChanged =
                ::applyBesserChallengeUpdate,

            onMessageObservationFailed = {
                showError(
                    message =
                        ChatPresentationTokens.SessionCoordinator.MessageLoadError
                )
            }
        )
    }

    private fun applyMessageUpdate(
        update: ChatMessagesUpdate
    ) {
        val provisionalId =
            provisionalGreetingMessageId

        if (
            provisionalId !=
            null &&
            update.messages.isEmpty()
        ) {
            return
        }

        if (
            provisionalId !=
            null &&
            update.messages.isNotEmpty()
        ) {
            provisionalGreetingMessageId =
                null
        }

        _uiState.update { state ->
            state.withMessages(
                messages =
                    update.messages
            )
        }
    }

    private fun applyBesserChallengeUpdate(
        challenges:
        Map<String, BesserChallengeUiModel>
    ) {
        _uiState.update { state ->
            state.copy(
                besserChallenges =
                    challenges
            )
        }
    }

    fun startBesserRefinement(
        challengeId: String
    ): Boolean {
        val currentState =
            _uiState.value

        if (
            currentState.isSubmittingBesserRefinement ||
            currentState.missionVariation.isStarting
        ) {
            return false
        }

        val challenge =
            currentState
                .besserChallenges
                .values
                .firstOrNull { candidate ->
                    candidate.id ==
                            challengeId
                }
                ?: return false

        val sourceMessage =
            currentState
                .messages
                .firstOrNull { message ->
                    message.id ==
                            challenge.messageId
                }
                ?: return false

        val sourceText =
            sourceMessage
                .content
                .trim()

        if (sourceText.isBlank()) {
            return false
        }

        val refinement =
            BesserRefinementUiState(
                challengeId =
                    challenge.id,

                sourceMessageId =
                    challenge.messageId,

                originalMessageContent =
                    sourceText,

                instruction =
                    challenge.instruction,

                capabilityId =
                    challenge.capabilityId,

                revealedHintCount =
                    challenge.revealedHintCount
            )

        _uiState.update { state ->
            state.besserRefinementStarted(
                refinement =
                    refinement,

                input =
                    TextFieldValue(
                        text =
                            sourceText,

                        selection =
                            TextRange(
                                sourceText.length
                            )
                    )
            )
        }

        return true
    }

    private fun cancelBesserRefinement() {
        if (
            _uiState
                .value
                .isSubmittingBesserRefinement
        ) {
            return
        }

        _uiState.update { state ->
            state.besserRefinementCancelled()
        }
    }

    fun showBesserError(
        message: String
    ) {
        showError(
            message =
                message
        )
    }

    private fun applyStreakUpdate(
        streakDays: Int
    ) {
        _uiState.update { state ->
            state.withCurrentStreak(
                streakDays =
                    streakDays
            )
        }
    }

    private fun showMissionResult(
        missionTitle: String,
        analytics: MissionAnalytics
    ) {
        missionResultController.showResult(
            missionTitle =
                missionTitle,

            analytics =
                analytics
        )
    }

    private fun checkForMissionOpportunity(
        conversationId: String,
        scope: CoroutineScope
    ) {
        missionController.checkForMissionOpportunity(
            conversationId =
                conversationId,

            scope =
                scope,

            runtime =
                runtime,

            uiState =
                _uiState
        )
    }

    private fun consumeAdaptiveLearningFeedback(
        eventId: String
    ) {
        _uiState.update { state ->
            if (
                state
                    .adaptiveLearningFeedbackEvent
                    ?.eventId !=
                eventId
            ) {
                state
            } else {
                state.copy(
                    adaptiveLearningFeedbackEvent =
                        null
                )
            }
        }
    }

    private fun dismissError() {
        _uiState.update { state ->
            state.errorDismissed()
        }
    }

    private fun showError(
        message: String
    ) {
        _uiState.update { state ->
            state.copy(
                errorMessage =
                    message
            )
        }
    }

}
