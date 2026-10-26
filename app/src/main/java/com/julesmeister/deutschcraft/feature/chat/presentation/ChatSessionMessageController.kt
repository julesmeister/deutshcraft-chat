package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatSessionMessageController @Inject constructor(
    private val messageSender:
    ChatMessageSender,
    private val suggestionHandler:
    ChatSuggestionHandler
) {

    fun updateInput(
        value: TextFieldValue,
        uiState: MutableStateFlow<ChatUiState>
    ) {
        if (uiState.value.isInitializing) {
            return
        }

        uiState.update { state ->
            state.withInput(
                value =
                    value
            )
        }
    }

    fun selectSuggestion(
        suggestionId: String,
        uiState: MutableStateFlow<ChatUiState>
    ) {
        val currentState =
            uiState.value

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

        uiState.update { state ->
            state.withInput(
                value =
                    updatedInput
            )
        }
    }

    fun send(
        scope: CoroutineScope,
        runtime: ChatSessionRuntime,
        uiState: MutableStateFlow<ChatUiState>,
        onMissionOpportunityCheckRequested:
            (String, CoroutineScope) -> Unit,
        onError: (String) -> Unit
    ) {
        val conversationId =
            runtime.activeConversationId
                ?: run {
                    onError(
                        CONVERSATION_LOADING_ERROR
                    )

                    return
                }

        val currentState =
            uiState.value

        val messageText =
            currentState
                .messageInput
                .text
                .trim()

        if (
            messageText.isBlank() ||
            currentState.isInitializing ||
            currentState.isSending ||
            runtime.sendingJob?.isActive == true
        ) {
            return
        }

        /*
         * Remove previous suggestions immediately after the
         * learner sends another message.
         */
        messageSender.clearSuggestions(
            conversationId =
                conversationId
        )

        uiState.update { state ->
            state.sendingStarted()
        }

        runtime.sendingJob =
            scope.launch {
                sendMessage(
                    conversationId =
                        conversationId,
                    messageText =
                        messageText,
                    scope =
                        scope,
                    runtime =
                        runtime,
                    uiState =
                        uiState,
                    onMissionOpportunityCheckRequested =
                        onMissionOpportunityCheckRequested
                )
            }
    }

    private suspend fun sendMessage(
        conversationId: String,
        messageText: String,
        scope: CoroutineScope,
        runtime: ChatSessionRuntime,
        uiState: MutableStateFlow<ChatUiState>,
        onMissionOpportunityCheckRequested:
            (String, CoroutineScope) -> Unit
    ) {
        try {
            /*
             * The complete active mission is passed to the
             * sender.
             *
             * ChatMessageSender then evaluates the exact
             * saved USER message against the unfinished
             * objectives after the normal chat send
             * succeeds.
             */
            val result =
                messageSender.send(
                    conversationId =
                        conversationId,
                    messageText =
                        messageText,
                    activeMission =
                        runtime.activeMission
                )

            uiState.update { state ->
                state.sendingSucceeded(
                    suggestions =
                        result.suggestions,
                    warning =
                        result.warning
                )
            }

            /*
             * New mission detection is only appropriate when
             * no mission is already active.
             */
            if (runtime.activeMission == null) {
                onMissionOpportunityCheckRequested(
                    conversationId,
                    scope
                )
            }
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                CONTROLLER_TAG,
                "Message sending failed.",
                throwable
            )

            messageSender.clearSuggestions(
                conversationId =
                    conversationId
            )

            uiState.update { state ->
                state.sendingFailed(
                    message =
                        SEND_ERROR
                )
            }
        } finally {
            runtime.sendingJob =
                null
        }
    }

    private companion object {
        const val CONTROLLER_TAG =
            "ChatSessionMessage"

        const val CONVERSATION_LOADING_ERROR =
            "The conversation is still loading."

        const val SEND_ERROR =
            "The assistant could not respond. " +
                    "Your message was saved locally."
    }
}