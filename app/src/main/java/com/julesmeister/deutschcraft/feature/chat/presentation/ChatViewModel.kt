package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.chat.data.FakeChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatUiState(
            messages = listOf(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    content = "Hallo! Wie war dein Tag?",
                    sender = MessageSender.ASSISTANT,
                    timestamp = System.currentTimeMillis()
                )
            )
        )
    )

    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onAction(action: ChatUiAction) {
        when (action) {
            is ChatUiAction.MessageInputChanged -> {
                updateMessageInput(action.value)
            }

            ChatUiAction.SendMessage -> {
                sendCurrentMessage()
            }

            is ChatUiAction.SuggestionSelected -> {
                selectSuggestion(action.suggestionId)
            }

            ChatUiAction.ErrorDismissed -> {
                dismissError()
            }

            ChatUiAction.RetryMessage -> {
                sendCurrentMessage()
            }

            ChatUiAction.ProgressClicked -> {
                // Navigation will be added later.
            }
        }
    }

    private fun updateMessageInput(value: String) {
        _uiState.update { currentState ->
            currentState.copy(messageInput = value)
        }
    }

    private fun selectSuggestion(suggestionId: String) {
        val suggestion = _uiState.value.suggestions
            .find { it.id == suggestionId }
            ?: return

        _uiState.update { currentState ->
            currentState.copy(messageInput = suggestion.text)
        }
    }

    private fun sendCurrentMessage() {
        val messageText = _uiState.value.messageInput.trim()

        if (messageText.isBlank()) {
            return
        }

        if (_uiState.value.isSending) {
            return
        }

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = messageText,
            sender = MessageSender.USER,
            timestamp = System.currentTimeMillis()
        )

        _uiState.update { currentState ->
            currentState.copy(
                messages = currentState.messages + userMessage,
                messageInput = "",
                suggestions = emptyList(),
                isSending = true,
                isAssistantTyping = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            runCatching {
                repository.sendMessage(messageText)
            }.onSuccess { reply ->
                _uiState.update { currentState ->
                    currentState.copy(
                        messages = currentState.messages + reply.message,
                        suggestions = reply.suggestions,
                        isSending = false,
                        isAssistantTyping = false
                    )
                }
            }.onFailure {
                _uiState.update { currentState ->
                    currentState.copy(
                        isSending = false,
                        isAssistantTyping = false,
                        errorMessage = "The message could not be sent."
                    )
                }
            }
        }
    }

    private fun dismissError() {
        _uiState.update { currentState ->
            currentState.copy(errorMessage = null)
        }
    }
}