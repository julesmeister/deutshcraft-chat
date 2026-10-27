package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * Controls the message composer and coordinates one complete
 * message-send operation.
 *
 * Responsibilities:
 *
 * - Update message input
 * - Apply selected suggestions
 * - Prevent duplicate sends
 * - Clear outdated suggestions
 * - Pass the active mission to ChatMessageSender
 * - Update ChatUiState after sending
 * - Forward completed mission analytics to the coordinator
 * - Request a new mission opportunity only when the send
 *   started without an active mission
 */
class ChatSessionMessageController
@Inject constructor(
    private val messageSender:
    ChatMessageSender,

    private val suggestionHandler:
    ChatSuggestionHandler
) {

    fun updateInput(
        value: TextFieldValue,
        uiState:
        MutableStateFlow<ChatUiState>
    ) {
        if (
            uiState.value.isInitializing
        ) {
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
        uiState:
        MutableStateFlow<ChatUiState>
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

    /*
     * Starts one message-send operation.
     *
     * onMissionCompleted is invoked only when:
     *
     * - the send started with an active mission
     * - the learner completed its final required objective
     * - final MissionAnalytics were calculated and persisted
     */
    fun send(
        scope: CoroutineScope,
        runtime: ChatSessionRuntime,
        uiState:
        MutableStateFlow<ChatUiState>,
        onMissionOpportunityCheckRequested:
            (
            conversationId: String,
            scope: CoroutineScope
        ) -> Unit,
        onMissionCompleted:
            (
            missionTitle: String,
            analytics: MissionAnalytics
        ) -> Unit,
        onError:
            (message: String) -> Unit
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
            runtime.sendingJob?.isActive ==
            true
        ) {
            return
        }

        /*
         * Remove suggestions belonging to the previous
         * assistant response as soon as the learner sends
         * another message.
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
                        onMissionOpportunityCheckRequested,
                    onMissionCompleted =
                        onMissionCompleted
                )
            }
    }

    private suspend fun sendMessage(
        conversationId: String,
        messageText: String,
        scope: CoroutineScope,
        runtime: ChatSessionRuntime,
        uiState:
        MutableStateFlow<ChatUiState>,
        onMissionOpportunityCheckRequested:
            (
            conversationId: String,
            scope: CoroutineScope
        ) -> Unit,
        onMissionCompleted:
            (
            missionTitle: String,
            analytics: MissionAnalytics
        ) -> Unit
    ) {
        try {
            /*
             * Capture the active mission before sending.
             *
             * When the final objective completes, the Room
             * mission observer may clear runtime.activeMission
             * before this method finishes.
             *
             * The snapshot lets post-send logic know whether
             * this send originally belonged to a mission.
             */
            val activeMissionAtSendStart =
                runtime.activeMission

            /*
             * ChatMessageSender performs the complete send and
             * post-send learning pipeline:
             *
             * - Persist learner message
             * - Generate and persist assistant response
             * - Evaluate the mission objective
             * - Persist interaction evidence
             * - Complete the objective when appropriate
             * - Finalize mission analytics when appropriate
             * - Run adaptive learning
             */
            val result =
                messageSender.send(
                    conversationId =
                        conversationId,
                    messageText =
                        messageText,
                    activeMission =
                        activeMissionAtSendStart
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
             * Forward finalized mission analytics to the
             * coordinator.
             *
             * Both values must exist together.
             */
            val completedMissionTitle =
                result.completedMissionTitle

            val completedMissionAnalytics =
                result.completedMissionAnalytics

            if (
                !completedMissionTitle.isNullOrBlank() &&
                completedMissionAnalytics !=
                null
            ) {
                onMissionCompleted(
                    completedMissionTitle,
                    completedMissionAnalytics
                )

                Log.i(
                    CONTROLLER_TAG,
                    buildString {
                        append(
                            "Completed mission forwarded to "
                        )
                        append(
                            "presentation. missionId="
                        )
                        append(
                            completedMissionAnalytics.missionId
                        )
                        append(
                            ", title="
                        )
                        append(
                            completedMissionTitle
                        )
                        append(
                            ", overall="
                        )
                        append(
                            completedMissionAnalytics.overallScore
                        )
                    }
                )
            }

            /*
             * Check for a new mission opportunity only if the
             * learner was not already performing a mission
             * when the send began.
             *
             * Do not inspect runtime.activeMission here.
             * Room may already have cleared a mission that
             * completed during this send.
             */
            if (
                activeMissionAtSendStart ==
                null
            ) {
                onMissionOpportunityCheckRequested(
                    conversationId,
                    scope
                )
            }

            Log.d(
                CONTROLLER_TAG,
                buildString {
                    append(
                        "Message send completed. "
                    )
                    append(
                        "conversationId="
                    )
                    append(
                        conversationId
                    )
                    append(
                        ", missionActiveAtStart="
                    )
                    append(
                        activeMissionAtSendStart !=
                                null
                    )
                    append(
                        ", missionCompleted="
                    )
                    append(
                        completedMissionAnalytics !=
                                null
                    )
                    append(
                        ", suggestionCount="
                    )
                    append(
                        result.suggestions.size
                    )
                    append(
                        ", hasWarning="
                    )
                    append(
                        !result.warning.isNullOrBlank()
                    )
                }
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            /*
             * Coroutine cancellation is not a normal send
             * failure and must propagate.
             */
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