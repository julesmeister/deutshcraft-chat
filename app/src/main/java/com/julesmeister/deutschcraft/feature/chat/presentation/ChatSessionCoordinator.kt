package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.chat.data.cache.ReplySuggestionCache
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.InitializeChatUseCase
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.SendChatMessageUseCase
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
    private val initializeChat:
    InitializeChatUseCase,
    private val sendChatMessage:
    SendChatMessageUseCase,
    private val sessionObservers:
    ChatSessionObservers,
    private val replySuggestionCache:
    ReplySuggestionCache
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

        initializationJob = null
        sendingJob = null
        activeConversationId = null

        /*
         * Do not clear ReplySuggestionCache here.
         *
         * stop() is called when leaving the chat. Clearing
         * the cache would recreate the original problem.
         */
        sessionObservers.stop()
    }

    private suspend fun initialize(
        requestedConversationId: String,
        scope: CoroutineScope
    ) {
        try {
            val conversation =
                initializeChat(
                    conversationId =
                        requestedConversationId
                )

            activeConversationId =
                conversation.id

            /*
             * Restore the temporary suggestions previously
             * generated for this conversation.
             *
             * The cache is a Hilt singleton, so it survives
             * navigation while the process remains alive.
             */
            restoreCachedSuggestions(
                conversationId =
                        conversation.id
            )

            sessionObservers.observeConversation(
                  conversationId =
                      conversation.id,
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

    private fun restoreCachedSuggestions(
        conversationId: String
    ) {
        val cachedSuggestions =
            replySuggestionCache
                .getSuggestions(
                    conversationId =
                        conversationId
                )

        _uiState.update { state ->
            state.copy(
                suggestions =
                    cachedSuggestions
            )
        }
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
        _uiState.update { state ->
            state.withActiveMission(
                mission =
                    update.mission,
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

        if (
            currentState.isInitializing ||
            currentState.isSending
        ) {
            return
        }

        val selectedSuggestion =
            currentState.suggestions
                .firstOrNull { suggestion ->
                    suggestion.id ==
                            suggestionId
                }
                ?: return

        val suggestedText =
            selectedSuggestion
                .text
                .capitalizeFirstCharacter()

        val suggestedInput =
            TextFieldValue(
                text =
                    suggestedText,
                selection =
                    TextRange(
                        index =
                            suggestedText.length
                    )
            )

        /*
         * Selecting a suggestion fills the composer but
         * does not consume the suggestions yet.
         *
         * They are consumed when the learner actually sends
         * the next message.
         */
        _uiState.update { state ->
            state.withInput(
                value =
                    suggestedInput
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

        /*
         * The existing suggestions belong to the previous
         * assistant response.
         *
         * Once a new learner message is sent, those
         * suggestions are no longer actionable.
         */
        replySuggestionCache.clearSuggestions(
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
                        messageText
                )
            }
    }

    private suspend fun sendMessage(
        conversationId: String,
        messageText: String
    ) {
        try {
            val result =
                sendChatMessage(
                    conversationId =
                        conversationId,
                    messageText =
                        messageText
                )

            val suggestions =
                result
                    .assistantReply
                    .suggestions

            /*
             * Store the latest generated suggestions in the
             * in-memory conversation cache.
             *
             * Returning to this chat during the same app
             * process will restore these suggestions.
             */
            replySuggestionCache.replaceSuggestions(
                conversationId =
                    conversationId,
                suggestions =
                    suggestions
            )

            _uiState.update { state ->
                state.sendingSucceeded(
                    suggestions =
                        suggestions,
                    warning =
                        result.assessmentWarning
                )
            }

            Log.d(
                COORDINATOR_TAG,
                "Send completed with " +
                        "${suggestions.size} suggestions."
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

            /*
             * Do not restore the old suggestions after a
             * failed send.
             *
             * The learner already attempted to answer the
             * previous assistant response, and their
             * message may have been saved locally.
             */
            replySuggestionCache.clearSuggestions(
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
            "The saved conversation could " +
                    "not be loaded."

        const val CONVERSATION_LOADING_ERROR =
            "The conversation is still loading."

        const val SEND_ERROR =
            "The assistant could not respond. " +
                    "Your message was saved locally."
    }
}