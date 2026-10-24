package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.core.cefr.engine.LocalMessageEvaluator
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.assessment.domain.ProcessAssessmentUseCase
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.progress.domain.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val progressRepository: ProgressRepository,
    private val localMessageEvaluator:
    LocalMessageEvaluator,
    private val processAssessment:
    ProcessAssessmentUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(ChatUiState())

    val uiState: StateFlow<ChatUiState> =
        _uiState.asStateFlow()

    private var conversationId: String? = null

    private var messagesObservationJob: Job? = null

    init {
        initializeChat()
    }

    fun onAction(
        action: ChatUiAction
    ) {
        when (action) {
            is ChatUiAction.MessageInputChanged -> {
                updateMessageInput(action.value)
            }

            ChatUiAction.SendMessage -> {
                sendCurrentMessage()
            }

            is ChatUiAction.SuggestionSelected -> {
                selectSuggestion(
                    suggestionId =
                        action.suggestionId
                )
            }

            ChatUiAction.ErrorDismissed -> {
                dismissError()
            }
        }
    }
    private fun initializeChat() {
        viewModelScope.launch {
            try {
                progressRepository.ensureProfileExists()

                val conversation =
                    chatRepository
                        .getOrCreatePrimaryConversation()

                conversationId = conversation.id

                observeMessages(
                    conversationId = conversation.id
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        isInitializing = false
                    )
                }
            } catch (
                cancellationException: CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isInitializing = false,
                        errorMessage =
                            "DeutschCraft could not load " +
                                    "the conversation."
                    )
                }
            }
        }
    }
    private fun observeMessages(
        conversationId: String
    ) {
        messagesObservationJob?.cancel()

        messagesObservationJob =
            viewModelScope.launch {
                try {
                    chatRepository
                        .observeMessages(conversationId)
                        .collect { messages ->
                            _uiState.update { state ->
                                state.copy(
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
                    showError(
                        message =
                            "The saved conversation " +
                                    "could not be loaded."
                    )
                }
            }
    }
    private fun updateMessageInput(
        value: String
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                messageInput = value
            )
        }
    }

    private fun selectSuggestion(
        suggestionId: String
    ) {
        if (_uiState.value.isSending) {
            return
        }

        val suggestion =
            _uiState.value.suggestions
                .firstOrNull { suggestion ->
                    suggestion.id == suggestionId
                }
                ?: return

        _uiState.update { state ->
            state.copy(
                messageInput = suggestion.text
            )
        }
    }
    private fun sendCurrentMessage() {
        /*
         * Read everything before modifying ChatUiState.
         */
        val activeConversationId = conversationId

        if (activeConversationId == null) {
            showError(
                message = "The conversation is still loading."
            )
            return
        }

        val currentState = _uiState.value
        val messageText = currentState.messageInput.trim()

        if (
            messageText.isBlank() ||
            currentState.isSending ||
            currentState.isInitializing
        ) {
            return
        }

        /*
         * Clear the composer and show the typing state
         * only after validating the message.
         */
        _uiState.update { state ->
            state.copy(
                messageInput = "",
                suggestions = emptyList(),
                isSending = true,
                isAssistantTyping = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val userMessage = createUserMessage(
                    conversationId = activeConversationId,
                    content = messageText
                )

                /*
                 * Room is the source of truth.
                 * Saving first makes the user message durable.
                 */
                chatRepository.saveMessage(userMessage)

                /*
                 * Assessment failure is handled internally and
                 * must not remove the user's saved message.
                 */
                processMessageAssessment(
                    userMessage = userMessage
                )

                val assistantReply =
                    chatRepository.generateAssistantReply(
                        conversationId =
                            activeConversationId,
                        message = messageText
                    )

                chatRepository.saveMessage(
                    assistantReply.message
                )

                _uiState.update { state ->
                    state.copy(
                        suggestions =
                            assistantReply.suggestions,
                        isSending = false,
                        isAssistantTyping = false
                    )
                }
            } catch (
                cancellationException: CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                _uiState.update { state ->
                    state.copy(
                        isSending = false,
                        isAssistantTyping = false,
                        errorMessage =
                            "The assistant could not respond. " +
                                    "Your message was saved locally."
                    )
                }
            }
        }
    }
    private suspend fun createUserMessage(
        conversationId: String,
        content: String
    ): ChatMessage {
        val sequenceNumber =
            chatRepository
                .getNextSequenceNumber(
                    conversationId
                )

        return ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            content = content,
            sender = MessageSender.USER,
            sequenceNumber = sequenceNumber,
            timestamp =
                System.currentTimeMillis()
        )
    }

    private suspend fun processMessageAssessment(
        userMessage: ChatMessage
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                isAssessing = true
            )
        }

        try {
            val currentProgress =
                progressRepository.getProgress()

            val currentLevel =
                currentProgress?.currentLevel
                    ?: CefrLevel.A1

            val activeDayCount =
                calculateActiveDayCount(
                    currentCount =
                        currentProgress
                            ?.activeDayCount
                            ?: 0
                )

            val localEvidence =
                localMessageEvaluator.evaluate(
                    messageId =
                        userMessage.id,
                    conversationId =
                        userMessage.conversationId,
                    content =
                        userMessage.content,
                    currentLevel =
                        currentLevel
                )

            processAssessment(
                evidence = localEvidence,
                activeDayCount = activeDayCount
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            /*
             * Assessment is secondary to chatting.
             *
             * The message remains saved in Room.
             * We do not fail the whole send operation
             * when local stats processing fails.
             */
            _uiState.update { currentState ->
                currentState.copy(
                    errorMessage =
                        "Your message was sent, but " +
                                "the progress stats could " +
                                "not be updated."
                )
            }
        } finally {
            _uiState.update { currentState ->
                currentState.copy(
                    isAssessing = false
                )
            }
        }
    }

    private fun calculateActiveDayCount(
        currentCount: Int
    ): Int {
        /*
         * Temporary implementation.
         *
         * This guarantees at least one active day,
         * but it must eventually be replaced by
         * an ActivityDayEntity or DataStore-based
         * date tracker so multiple messages on the
         * same date do not increment the count.
         */
        return currentCount.coerceAtLeast(1)
    }

    private fun dismissError() {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = null
            )
        }
    }

    private fun showError(
        message: String
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = message,
                isSending = false,
                isAssistantTyping = false,
                isAssessing = false
            )
        }
    }
}