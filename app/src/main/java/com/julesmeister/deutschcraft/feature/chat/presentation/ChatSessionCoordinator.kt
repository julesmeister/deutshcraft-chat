package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.feature.besser.presentation.BesserChallengeUiModel
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result.MissionResultController
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

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

    val uiState:
            StateFlow<ChatUiState> =
        _uiState.asStateFlow()

    private val runtime =
        ChatSessionRuntime()

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
                messageController.selectSuggestion(
                    suggestionId =
                        action.suggestionId,
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
                missionController
                    .consumeObjectiveCompletion(
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

    fun stop() {
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

            val cachedMissionOffer =
                missionController.restoreOffer(
                    conversationId =
                        initializationResult
                            .conversationId
                )

            _uiState.update { state ->
                state.copy(
                    suggestions =
                        initializationResult
                            .cachedSuggestions,
                    missionOffer =
                        cachedMissionOffer
                )
            }

            observeConversation(
                conversationId =
                    initializationResult
                        .conversationId,
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
                COORDINATOR_TAG,
                "Chat initialization failed.",
                throwable
            )

            _uiState.update { state ->
                state.initializationFailed(
                    message =
                        throwable.message
                            ?: LOAD_ERROR
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
                        MESSAGE_LOAD_ERROR
                )
            }
        )
    }

    private fun applyMessageUpdate(
        update: ChatMessagesUpdate
    ) {
        _uiState.update { state ->
            state.withMessages(
                messages =
                    update.messages
            )
        }
    }

    /*
     * =======================================================
     * Besser
     * =======================================================
     *
     * Room remains the source of truth.
     *
     * Once GenerateAndPersistBesserChallengeUseCase saves a
     * challenge, the Besser observer emits automatically and
     * updates the correct messageId entry here.
     */
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

        Log.i(
            COORDINATOR_TAG,
            buildString {
                append(
                    "Showing completed mission result. "
                )
                append(
                    "missionId="
                )
                append(
                    analytics.missionId
                )
                append(
                    ", overall="
                )
                append(
                    analytics.overallScore
                )
            }
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
            val currentEvent =
                state.adaptiveLearningFeedbackEvent

            if (
                currentEvent?.eventId !=
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

    private companion object {

        const val COORDINATOR_TAG =
            "ChatSessionCoordinator"

        const val LOAD_ERROR =
            "DeutschCraft could not load " +
                    "the conversation."

        const val MESSAGE_LOAD_ERROR =
            "The saved conversation could not " +
                    "be loaded."
    }

    fun startBesserRefinement(
        challengeId: String
    ): Boolean {
        val currentState =
            _uiState.value

        val challenge =
            currentState
                .besserChallenges
                .values
                .firstOrNull { challenge ->
                    challenge.id ==
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
            sourceMessage.content.trim()

        if (sourceText.isBlank()) {
            return false
        }

        _uiState.update { state ->
            state.copy(
                messageInput =
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

        Log.i(
            COORDINATOR_TAG,
            buildString {
                append(
                    "Started Besser refinement. "
                )
                append(
                    "challengeId="
                )
                append(
                    challengeId
                )
                append(
                    ", messageId="
                )
                append(
                    challenge.messageId
                )
            }
        )

        return true
    }

    fun showBesserError(
        message: String
    ) {
        showError(
            message =
                message
        )
    }
}