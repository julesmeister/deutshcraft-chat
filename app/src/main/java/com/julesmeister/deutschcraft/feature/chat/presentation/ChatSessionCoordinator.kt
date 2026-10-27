package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
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

    /*
     * Runtime state remains separate from visible UI state.
     *
     * It contains:
     *
     * - Active conversation ID
     * - Complete active mission
     * - Initialization job
     * - Message-sending job
     * - Mission-check job
     * - Mission-action job
     */
    private val runtime =
        ChatSessionRuntime()

    /*
     * Starts the chat session once.
     */
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

    /*
     * Routes UI actions to the controller responsible for
     * that behavior.
     */
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

                    /*
                     * When message processing completes the
                     * entire mission, the message controller
                     * forwards the finalized analytics here.
                     */
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

    /*
     * Stops every job and observer associated with the
     * currently opened conversation.
     */
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

            /*
             * Start observing messages and the active
             * Room-backed mission.
             */
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
            onMessageObservationFailed = {
                showError(
                    message =
                        MESSAGE_LOAD_ERROR
                )
            }
        )
    }

    /*
     * Applies message-list changes emitted by Room.
     */
    private fun applyMessageUpdate(
        update:
        ChatMessagesUpdate
    ) {
        _uiState.update { state ->
            state.withMessages(
                messages =
                    update.messages
            )
        }
    }

    /*
     * Applies streak changes from the existing observer.
     */
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

    /*
     * Receives finalized analytics when a learner message
     * completes the mission.
     *
     * At this point analytics have already been calculated
     * and persisted.
     */
    private fun showMissionResult(
        missionTitle: String,
        analytics:
        MissionAnalytics
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

    /*
     * Called after a normal message succeeds when no active
     * mission exists.
     */
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

    /*
     * Removes adaptive-learning feedback only when the event
     * ID matches the currently displayed event.
     */
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
}