package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatSessionCoordinator @Inject constructor(
    private val sessionInitializer:
    ChatSessionInitializer,
    private val sessionObservers:
    ChatSessionObservers,
    private val messageController:
    ChatSessionMessageController,
    private val missionController:
    ChatSessionMissionController
) {

    private val _uiState =
        MutableStateFlow(
            ChatUiState()
        )

    val uiState: StateFlow<ChatUiState> =
        _uiState.asStateFlow()

    /*
     * Runtime state is separate from visible UI state.
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
     *
     * The coordinator first observes the global streak,
     * then initializes the requested conversation.
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
     * Routes presentation actions to the controller that
     * owns the corresponding behavior.
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

            ChatUiAction.ErrorDismissed -> {
                dismissError()
            }
        }
    }

    /*
     * Stops every job and observer associated with the
     * currently opened conversation.
     *
     * The active mission is also cleared so it cannot leak
     * into another conversation.
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
             *
             * When the mission observer emits, the complete
             * ConversationMission is placed in runtime so it
             * can be supplied to ChatMessageSender.
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
     * Applies message-list updates emitted by Room.
     */
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
     * Applies streak updates from the existing streak
     * observer.
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
     * Called by ChatSessionMessageController after a normal
     * message succeeds and no active mission exists.
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