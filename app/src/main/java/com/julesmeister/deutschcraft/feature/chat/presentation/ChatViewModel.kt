package com.julesmeister.deutschcraft.feature.chat.presentation
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.core.activity.ActivityTracker
import com.julesmeister.deutschcraft.core.cefr.engine.LocalMessageEvaluator
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.cefr.model.DefaultLearnerProfile
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
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.julesmeister.deutschcraft.app.navigation.ChatDestination

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val progressRepository: ProgressRepository,
    private val localMessageEvaluator:
    LocalMessageEvaluator,
    private val processAssessment:
    ProcessAssessmentUseCase,
    private val activityTracker:
    ActivityTracker
) : ViewModel() {

    private val destination: ChatDestination =
        savedStateHandle.toRoute()

    private val selectedConversationId =
        destination.conversationId
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
                updateMessageInput(
                    value = action.value
                )
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
                        .getOrCreateConversation(
                            conversationId =
                                selectedConversationId
                        )

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
                cancellationException:
                CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.e(
                    CHAT_VIEW_MODEL_TAG,
                    "Failed to initialize conversation " +
                            selectedConversationId,
                    throwable
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        isInitializing = false,
                        errorMessage =
                            throwable.message
                                ?: ("DeutschCraft could not " +
                                        "load the conversation.")
                    )
                }
            }
        }
    }

    private fun observeMessages(
        conversationId: String
    ) {
        /*
         * Prevent multiple collectors from observing
         * different conversations simultaneously.
         */
        messagesObservationJob?.cancel()

        messagesObservationJob =
            viewModelScope.launch {
                try {
                    chatRepository
                        .observeMessages(
                            conversationId =
                                conversationId
                        )
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
        /*
         * Ignore composer updates while the screen
         * is still initializing.
         */
        if (_uiState.value.isInitializing) {
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                messageInput = value
            )
        }
    }

    private fun selectSuggestion(
        suggestionId: String
    ) {
        /*
         * Prevent a suggestion from replacing the
         * input during an active send operation.
         */
        if (
            _uiState.value.isSending ||
            _uiState.value.isInitializing
        ) {
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
         * Read and validate the current state before
         * clearing the message composer.
         */
        val activeConversationId =
            conversationId

        if (activeConversationId == null) {
            showError(
                message =
                    "The conversation is still loading."
            )
            return
        }

        val currentState = _uiState.value

        val messageText =
            currentState.messageInput.trim()

        if (
            messageText.isBlank() ||
            currentState.isSending ||
            currentState.isInitializing
        ) {
            return
        }

        /*
         * Clear the input only after capturing the
         * validated message text.
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
                val userMessage =
                    createUserMessage(
                        conversationId =
                            activeConversationId,
                        content = messageText
                    )

                /*
                 * Room is the source of truth.
                 *
                 * The user message is saved before
                 * assessment or response generation.
                 */
                chatRepository.saveMessage(
                    message = userMessage
                )

                /*
                 * Record today's message activity.
                 *
                 * Multiple messages on the same day
                 * update one ActivityDayEntity row,
                 * so they count as only one active day.
                 */
                val activeDayCount =
                    activityTracker
                        .recordMessageActivity(
                            profileId =
                                DefaultLearnerProfile
                                    .LOCAL_PROFILE_ID
                        )

                /*
                 * Assessment failure is handled
                 * internally and does not remove the
                 * locally saved user message.
                 */
                processMessageAssessment(
                    userMessage = userMessage,
                    activeDayCount =
                        activeDayCount
                )

                /*
                 * Currently this generates a fake local
                 * reply. Later this repository call can
                 * delegate to Spring Boot and Qwen.
                 */
                val assistantReply =
                    chatRepository
                        .generateAssistantReply(
                            conversationId =
                                activeConversationId,
                            message = messageText
                        )

                chatRepository.saveMessage(
                    message =
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
                cancellationException:
                CancellationException
            ) {
                /*
                 * Cancellation must be propagated so
                 * structured concurrency still works.
                 */
                throw cancellationException
            } catch (throwable: Throwable) {
                _uiState.update { state ->
                    state.copy(
                        isSending = false,
                        isAssistantTyping = false,
                        isAssessing = false,
                        errorMessage =
                            "The assistant could not " +
                                    "respond. Your message " +
                                    "was saved locally."
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
                    conversationId =
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
        userMessage: ChatMessage,
        activeDayCount: Int
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                isAssessing = true
            )
        }

        try {
            /*
             * The current profile determines which
             * CEFR-level rules should be used.
             */
            val currentProgress =
                progressRepository.getProgress()

            val currentLevel =
                currentProgress?.currentLevel
                    ?: CefrLevel.A1

            /*
             * The local evaluator creates conservative,
             * low-confidence evidence from observable
             * characteristics of the message.
             */
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

            /*
             * The use case runs the CEFR engine and
             * saves evidence, scores, and the updated
             * learner profile in Room.
             */
            processAssessment(
                evidence = localEvidence,
                activeDayCount = activeDayCount
            )

            /*
             * Optional but useful: record that an
             * assessment was completed today.
             *
             * This updates assessmentCount on today's
             * ActivityDayEntity. It does not create
             * another active day.
             */
            activityTracker.recordAssessmentActivity(
                profileId =
                    DefaultLearnerProfile
                        .LOCAL_PROFILE_ID
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            /*
             * Assessment is secondary to chat.
             *
             * The message remains available in Room,
             * and assistant response generation can
             * continue.
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
                isInitializing = false,
                isSending = false,
                isAssistantTyping = false,
                isAssessing = false
            )
        }
    }

    override fun onCleared() {
        messagesObservationJob?.cancel()
        super.onCleared()
    }
}
private const val CHAT_VIEW_MODEL_TAG =
    "ChatViewModel"
