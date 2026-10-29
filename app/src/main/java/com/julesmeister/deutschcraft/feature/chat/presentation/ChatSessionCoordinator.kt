package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.besser.presentation.BesserChallengeUiModel
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

    private val runtime =
        ChatSessionRuntime()

    /*
     * Non-null while the current screen is showing an
     * in-memory greeting for a conversation that does not yet
     * exist in Room.
     */
    private var provisionalGreetingMessageId:
            String? =
        null

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

                    onMissionCompleted =
                        ::showMissionResult,

                    onError =
                        ::showError
                )
            }

            ChatUiAction.CancelBesserRefinement -> {
                cancelBesserRefinement()
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

    fun stop() {
        provisionalGreetingMessageId =
            null

        runtime.clear()

        sessionObservers.stop()
    }

    /*
     * =======================================================
     * INITIALIZATION
     * =======================================================
     */

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

            provisionalGreetingMessageId =
                initializationResult
                    .provisionalMessages
                    .firstOrNull()
                    ?.id

            val cachedMissionOffer =
                missionController.restoreOffer(
                    conversationId =
                        initializationResult
                            .conversationId
                )

            _uiState.update { state ->
                state.copy(
                    messages =
                        initializationResult
                            .provisionalMessages,

                    suggestions =
                        initializationResult
                            .cachedSuggestions,

                    missionOffer =
                        cachedMissionOffer
                )
            }

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

            Log.d(
                COORDINATOR_TAG,
                buildString {
                    append(
                        "Chat initialized. "
                    )

                    append(
                        "conversationId="
                    )

                    append(
                        initializationResult
                            .conversationId
                    )

                    append(
                        ", provisional="
                    )

                    append(
                        initializationResult
                            .isProvisional
                    )
                }
            )
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

            provisionalGreetingMessageId =
                null

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

    /*
     * =======================================================
     * OBSERVATION
     * =======================================================
     */

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

            onBesserChallengesChanged =
                ::applyBesserChallengeUpdate,

            onMessageObservationFailed = {
                showError(
                    message =
                        MESSAGE_LOAD_ERROR
                )
            }
        )
    }

    /*
     * =======================================================
     * MESSAGE UPDATES
     * =======================================================
     */

    private fun applyMessageUpdate(
        update: ChatMessagesUpdate
    ) {
        val currentProvisionalGreetingId =
            provisionalGreetingMessageId

        if (
            currentProvisionalGreetingId !=
            null &&
            update.messages.isEmpty()
        ) {
            return
        }

        if (
            currentProvisionalGreetingId !=
            null &&
            update.messages.isNotEmpty()
        ) {
            provisionalGreetingMessageId =
                null

            Log.d(
                COORDINATOR_TAG,
                buildString {
                    append(
                        "Provisional greeting replaced by "
                    )

                    append(
                        "persisted conversation messages. "
                    )

                    append(
                        "persistedMessageCount="
                    )

                    append(
                        update.messages.size
                    )
                }
            )
        }

        _uiState.update { state ->
            state.withMessages(
                messages =
                    update.messages
            )
        }
    }

    /*
     * =======================================================
     * BESSER
     * =======================================================
     */

    private fun applyBesserChallengeUpdate(
        challenges:
        Map<String, BesserChallengeUiModel>
    ) {
        _uiState.update { state ->
            state.copy(
                besserChallenges =
                    challenges
            )
        }
    }

    /*
     * =======================================================
     * BESSER REFINEMENT
     * =======================================================
     */

    fun startBesserRefinement(
        challengeId: String
    ): Boolean {
        val currentState =
            _uiState.value

        /*
         * Do not replace a refinement currently being
         * submitted.
         */
        if (
            currentState
                .isSubmittingBesserRefinement
        ) {
            return false
        }

        val challenge =
            currentState
                .besserChallenges
                .values
                .firstOrNull { challenge ->
                    challenge.id ==
                            challengeId
                }
                ?: return false

        val sourceMessage =
            currentState
                .messages
                .firstOrNull { message ->
                    message.id ==
                            challenge.messageId
                }
                ?: return false

        val sourceText =
            sourceMessage
                .content
                .trim()

        if (sourceText.isBlank()) {
            return false
        }

        val refinement =
            BesserRefinementUiState(
                challengeId =
                    challenge.id,

                sourceMessageId =
                    challenge.messageId,

                originalMessageContent =
                    sourceText,

                instruction =
                    challenge.instruction,

                capabilityId =
                    challenge.capabilityId,

                revealedHintCount =
                    challenge.revealedHintCount
            )

        _uiState.update { state ->
            state.besserRefinementStarted(
                refinement =
                    refinement,

                input =
                    TextFieldValue(
                        text =
                            sourceText,

                        selection =
                            TextRange(
                                sourceText.length
                            )
                    )
            )
        }

        Log.i(
            COORDINATOR_TAG,
            buildString {
                append(
                    "Started Besser refinement. "
                )

                append(
                    "challengeId="
                )

                append(
                    challenge.id
                )

                append(
                    ", messageId="
                )

                append(
                    challenge.messageId
                )

                append(
                    ", capabilityId="
                )

                append(
                    challenge.capabilityId
                )
            }
        )

        return true
    }

    private fun cancelBesserRefinement() {
        val currentState =
            _uiState.value

        if (
            currentState
                .isSubmittingBesserRefinement
        ) {
            return
        }

        val refinement =
            currentState
                .activeBesserRefinement
                ?: return

        _uiState.update { state ->
            state.besserRefinementCancelled()
        }

        Log.d(
            COORDINATOR_TAG,
            buildString {
                append(
                    "Cancelled Besser refinement. "
                )

                append(
                    "challengeId="
                )

                append(
                    refinement.challengeId
                )
            }
        )
    }

    fun showBesserError(
        message: String
    ) {
        showError(
            message =
                message
        )
    }

    /*
     * =======================================================
     * STREAK
     * =======================================================
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
     * =======================================================
     * MISSION RESULT
     * =======================================================
     */

    private fun showMissionResult(
        missionTitle: String,
        analytics: MissionAnalytics
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
     * =======================================================
     * ADAPTIVE LEARNING
     * =======================================================
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

    /*
     * =======================================================
     * ERROR
     * =======================================================
     */

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