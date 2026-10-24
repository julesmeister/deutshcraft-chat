package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {
    private var conversationId: String? = null;
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    init {
        initializeConversation();
    }

    private fun initializeConversation() {
        viewModelScope.launch {
            val conversation =
                repository.getOrCreatePrimaryConversation()

            conversationId = conversation.id

            repository.observeMessages(conversation.id)
                .collect { messages ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            messages = messages
                        )
                    }
                }
        }
    }

    private val _uiState = MutableStateFlow(
        ChatUiState(
            messages = listOf(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    content = "Hallo! Wie war dein Tag?",
                    sender = MessageSender.ASSISTANT,
                    timestamp = System.currentTimeMillis(),
                    sequenceNumber = _messages.value.size.toLong(),
                    conversationId = conversationId ?: ""
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
        }
    }

    private fun updateMessageInput(value: String) {
        _uiState.update { currentState ->
            currentState.copy(messageInput = value)
        }
    }

    private fun selectSuggestion(suggestionId: String) {
        val suggestion = _uiState.value.suggestions
            .firstOrNull { it.id == suggestionId }
            ?: return

        _uiState.update { currentState ->
            currentState.copy(
                messageInput = suggestion.text
            )
        }
    }

    private fun sendCurrentMessage() {
        val activeConversationId =
            conversationId ?: return

        val messageText =
            _uiState.value.messageInput.trim()

        if (
            messageText.isBlank() ||
            _uiState.value.isSending
        ) {
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                messageInput = "",
                suggestions = emptyList(),
                isSending = true,
                isAssistantTyping = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            runCatching {
                val userSequence =
                    repository.getNextSequenceNumber(
                        activeConversationId
                    )

                val userMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    conversationId = activeConversationId,
                    content = messageText,
                    sender = MessageSender.USER,
                    sequenceNumber = userSequence,
                    timestamp = System.currentTimeMillis()
                )

                repository.saveMessage(userMessage)

                val reply =
                    repository.generateAssistantReply(
                        conversationId =
                            activeConversationId,
                        message = messageText
                    )

                repository.saveMessage(reply.message)

                reply
            }.onSuccess { reply ->
                _uiState.update { currentState ->
                    currentState.copy(
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
                        errorMessage =
                            "The message could not be sent."
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