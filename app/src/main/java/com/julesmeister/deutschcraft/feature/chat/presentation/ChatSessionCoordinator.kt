package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatSessionCoordinator @Inject constructor(
    private val sessionInitializer:
    ChatSessionInitializer,
    private val messageSender:
    ChatMessageSender,
    private val sessionObservers:
    ChatSessionObservers,
    private val suggestionHandler:
    ChatSuggestionHandler,
    private val missionOfferCoordinator:
    ChatMissionOfferCoordinator
) {

    private val _uiState =
        MutableStateFlow(
            ChatUiState()
        )

    val uiState: StateFlow<ChatUiState> =
        _uiState.asStateFlow()

    private var activeConversationId: String? =
        null

    private var initializationJob: Job? =
        null

    private var sendingJob: Job? =
        null

    private var missionCheckJob: Job? =
        null

    private var missionActionJob: Job? =
        null

    fun start(
        conversationId: String,
        scope: CoroutineScope
    ) {
        if (
            initializationJob?.isActive == true ||
            activeConversationId != null
        ) {
            return
        }

        sessionObservers.observeStreak(
            scope =
                scope,
            onStreakChanged =
                ::applyStreakUpdate
        )

        initializationJob =
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
                updateInput(
                    value =
                        action.value
                )
            }

            is ChatUiAction.SuggestionSelected -> {
                selectSuggestion(
                    suggestionId =
                        action.suggestionId
                )
            }

            ChatUiAction.SendMessage -> {
                send(
                    scope =
                        scope
                )
            }

            ChatUiAction.StartOfferedMission -> {
                startOfferedMission(
                    scope =
                        scope
                )
            }

            ChatUiAction.DismissMissionOffer -> {
                dismissMissionOffer()
            }

            is ChatUiAction.ObjectiveCompletionShown -> {
                consumeObjectiveCompletion(
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
        initializationJob?.cancel()
        sendingJob?.cancel()
        missionCheckJob?.cancel()
        missionActionJob?.cancel()

        initializationJob =
            null

        sendingJob =
            null

        missionCheckJob =
            null

        missionActionJob =
            null

        activeConversationId =
            null

        sessionObservers.stop()
    }

    private suspend fun initialize(
        requestedConversationId: String,
        scope: CoroutineScope
    ) {
        try {
            val result =
                sessionInitializer.initialize(
                    requestedConversationId =
                        requestedConversationId
                )

            activeConversationId =
                result.conversationId

            val cachedMissionOffer =
                missionOfferCoordinator.restoreOffer(
                    conversationId =
                        result.conversationId
                )

            _uiState.update { state ->
                state.copy(
                    suggestions =
                        result.cachedSuggestions,
                    missionOffer =
                        cachedMissionOffer
                )
            }

            observeConversation(
                conversationId =
                    result.conversationId,
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
            onMissionChanged =
                ::applyMissionUpdate,
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

    private fun applyMissionUpdate(
        update: ChatMissionUpdate
    ) {
        val mission =
            update.mission

        if (mission != null) {
            val conversationId =
                activeConversationId

            if (conversationId != null) {
                missionOfferCoordinator.clearOffer(
                    conversationId =
                        conversationId
                )
            }

            missionCheckJob?.cancel()
        }

        _uiState.update { state ->
            state.withActiveMission(
                mission =
                    mission,
                newlyCompletedObjective =
                    update.newlyCompletedObjective
            )
        }
    }

    private fun updateInput(
        value: TextFieldValue
    ) {
        if (_uiState.value.isInitializing) {
            return
        }

        _uiState.update { state ->
            state.withInput(
                value =
                    value
            )
        }
    }

    private fun selectSuggestion(
        suggestionId: String
    ) {
        val currentState =
            _uiState.value

        val updatedInput =
            suggestionHandler
                .createInputFromSuggestion(
                    suggestions =
                        currentState.suggestions,
                    suggestionId =
                        suggestionId,
                    isEnabled =
                        !currentState.isInitializing &&
                                !currentState.isSending
                )
                ?: return

        _uiState.update { state ->
            state.withInput(
                value =
                    updatedInput
            )
        }
    }

    private fun send(
        scope: CoroutineScope
    ) {
        val conversationId =
            activeConversationId
                ?: run {
                    showError(
                        message =
                            CONVERSATION_LOADING_ERROR
                    )

                    return
                }

        val currentState =
            _uiState.value

        val messageText =
            currentState
                .messageInput
                .text
                .trim()

        if (
            messageText.isBlank() ||
            currentState.isInitializing ||
            currentState.isSending ||
            sendingJob?.isActive == true
        ) {
            return
        }

        messageSender.clearSuggestions(
            conversationId =
                conversationId
        )

        _uiState.update { state ->
            state.sendingStarted()
        }

        sendingJob =
            scope.launch {
                sendMessage(
                    conversationId =
                        conversationId,
                    messageText =
                        messageText,
                    scope =
                        scope
                )
            }
    }

    private suspend fun sendMessage(
        conversationId: String,
        messageText: String,
        scope: CoroutineScope
    ) {
        try {
            val result =
                messageSender.send(
                    conversationId =
                        conversationId,
                    messageText =
                        messageText
                )

            _uiState.update { state ->
                state.sendingSucceeded(
                    suggestions =
                        result.suggestions,
                    warning =
                        result.warning
                )
            }

            /*
             * Mission detection begins only after the normal
             * assistant response succeeds.
             *
             * It runs in a separate coroutine so the chat
             * response remains the primary experience.
             */
            checkForMissionOpportunity(
                conversationId =
                    conversationId,
                scope =
                    scope
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                COORDINATOR_TAG,
                "Message sending failed.",
                throwable
            )

            messageSender.clearSuggestions(
                conversationId =
                    conversationId
            )

            _uiState.update { state ->
                state.sendingFailed(
                    message =
                        SEND_ERROR
                )
            }
        }
    }

    private fun checkForMissionOpportunity(
        conversationId: String,
        scope: CoroutineScope
    ) {
        val currentState =
            _uiState.value

        if (
            currentState.activeMissionId != null ||
            currentState.missionOffer != null ||
            currentState.isCheckingMissionOpportunity ||
            missionCheckJob?.isActive == true
        ) {
            return
        }

        _uiState.update { state ->
            state.missionCheckStarted()
        }

        missionCheckJob =
            scope.launch {
                try {
                    val offer =
                        missionOfferCoordinator
                            .detectOffer(
                                conversationId =
                                    conversationId,
                                hasActiveMission =
                                    _uiState.value
                                        .activeMissionId != null
                            )

                    _uiState.update { state ->
                        state.missionCheckFinished(
                            offer =
                                offer
                        )
                    }
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    /*
                     * Mission detection must stay silent.
                     * Chat has already completed successfully.
                     */
                    Log.e(
                        COORDINATOR_TAG,
                        "Mission check failed.",
                        throwable
                    )

                    _uiState.update { state ->
                        state.missionCheckFinished(
                            offer =
                                null
                        )
                    }
                }
            }
    }

    private fun startOfferedMission(
        scope: CoroutineScope
    ) {
        val conversationId =
            activeConversationId
                ?: return

        val currentState =
            _uiState.value

        if (
            currentState.missionOffer == null ||
            currentState.isStartingMission ||
            currentState.activeMissionId != null ||
            missionActionJob?.isActive == true
        ) {
            return
        }

        _uiState.update { state ->
            state.missionStartStarted()
        }

        missionActionJob =
            scope.launch {
                try {
                    missionOfferCoordinator
                        .acceptOffer(
                            conversationId =
                                conversationId
                        )

                    /*
                     * Room observation will provide the full
                     * active mission. The local offer can be
                     * removed immediately.
                     */
                    _uiState.update { state ->
                        state.missionStartSucceeded()
                    }
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    Log.e(
                        COORDINATOR_TAG,
                        "Mission could not be started.",
                        throwable
                    )

                    _uiState.update { state ->
                        state.missionStartFailed(
                            message =
                                MISSION_START_ERROR
                        )
                    }
                }
            }
    }

    private fun dismissMissionOffer() {
        val conversationId =
            activeConversationId
                ?: return

        missionOfferCoordinator.dismissOffer(
            conversationId =
                conversationId
        )

        _uiState.update { state ->
            state.missionOfferDismissed()
        }
    }

    private fun consumeObjectiveCompletion(
        eventId: String
    ) {
        _uiState.update { state ->
            state.objectiveCompletionShown(
                eventId =
                    eventId
            )
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

        const val CONVERSATION_LOADING_ERROR =
            "The conversation is still loading."

        const val SEND_ERROR =
            "The assistant could not respond. " +
                    "Your message was saved locally."

        const val MISSION_START_ERROR =
            "The mission could not be started."
    }
}