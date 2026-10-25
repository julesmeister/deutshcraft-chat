package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.InitializeChatUseCase
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.ObserveChatMessagesUseCase
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
    private val observeChatMessages:
    ObserveChatMessagesUseCase,
    private val sendChatMessage:
    SendChatMessageUseCase
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

    private var observationJob: Job? =
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
                    value = action.value
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
                    scope = scope
                )
            }

            ChatUiAction.ErrorDismissed -> {
                dismissError()
            }
        }
    }

    fun stop() {
        initializationJob?.cancel()
        observationJob?.cancel()
        sendingJob?.cancel()

        initializationJob = null
        observationJob = null
        sendingJob = null
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

            observeMessages(
                conversationId =
                    conversation.id,
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

    private fun observeMessages(
        conversationId: String,
        scope: CoroutineScope
    ) {
        observationJob?.cancel()

        observationJob =
            scope.launch {
                try {
                    observeChatMessages(
                        conversationId =
                            conversationId
                    ).collect { messages ->
                        /*
                         * Messages and their persisted
                         * corrections now come directly
                         * from Room.
                         *
                         * Do not modify the composer or
                         * temporary suggestions here.
                         */
                        _uiState.update { state ->
                            state.withMessages(
                                messages = messages
                            )
                        }
                    }
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    Log.e(
                        COORDINATOR_TAG,
                        "Message observation failed.",
                        throwable
                    )

                    showError(
                        message =
                            MESSAGE_LOAD_ERROR
                    )
                }
            }
    }

    private fun updateInput(
        value: TextFieldValue
    ) {
        if (_uiState.value.isInitializing) {
            return
        }

        /*
         * Typing must not remove suggested replies.
         */
        _uiState.update { state ->
            state.withInput(
                value = value
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

        /*
         * TextRange at the final character position keeps
         * the cursor at the end of the inserted suggestion.
         */
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

        _uiState.update { state ->
            state.withInput(
                value = suggestedInput
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
         * Suggestions answer the previous assistant
         * message, so clear them only when the learner
         * sends the next message.
         */
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
             * The Besser correction is not handled here.
             *
             * SendChatMessageUseCase persists it directly
             * into the original user-message row. Room then
             * emits that updated ChatMessage automatically.
             */
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

            _uiState.update { state ->
                state.sendingFailed(
                    message =
                        SEND_ERROR
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
            "The saved conversation could " +
                    "not be loaded."

        const val CONVERSATION_LOADING_ERROR =
            "The conversation is still loading."

        const val SEND_ERROR =
            "The assistant could not respond. " +
                    "Your message was saved locally."
    }
}

private fun String.capitalizeFirstCharacter():
        String {
    val firstVisibleCharacterIndex =
        indexOfFirst { character ->
            !character.isWhitespace()
        }

    if (firstVisibleCharacterIndex == -1) {
        return this
    }

    val firstCharacter =
        this[firstVisibleCharacterIndex]

    if (firstCharacter.isUpperCase()) {
        return this
    }

    return replaceRange(
        range =
            firstVisibleCharacterIndex..
                    firstVisibleCharacterIndex,
        replacement =
            firstCharacter.uppercase()
    )
}