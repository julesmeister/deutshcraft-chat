package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.besser.domain.AssistanceLevel
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.CompleteBesserChallengeUseCase
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatSessionMessageController
@Inject constructor(
    private val messageSender:
    ChatMessageSender,

    private val suggestionHandler:
    ChatSuggestionHandler,

    private val completeBesserChallengeUseCase:
    CompleteBesserChallengeUseCase
) {

    fun updateInput(
        value: TextFieldValue,
        uiState:
        MutableStateFlow<ChatUiState>
    ) {
        val currentState =
            uiState.value

        if (
            currentState.isInitializing ||
            currentState.isSubmittingBesserRefinement
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

        /*
         * Reply suggestions belong to ordinary conversation,
         * never to an active Besser refinement.
         */
        if (
            currentState.isBesserRefinementMode
        ) {
            return
        }

        val updatedInput =
            suggestionHandler
                .createInputFromSuggestion(
                    suggestions =
                        currentState.suggestions,

                    suggestionId =
                        suggestionId,

                    isEnabled =
                        !currentState.isInitializing &&
                                !currentState.isSending &&
                                !currentState
                                    .isSubmittingBesserRefinement
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
            currentState.isSubmittingBesserRefinement ||
            runtime.sendingJob?.isActive ==
            true
        ) {
            return
        }

        /*
         * ===================================================
         * BESSER REFINEMENT SEND
         * ===================================================
         *
         * This branch occurs BEFORE the normal chat pipeline.
         *
         * Therefore a refinement does not:
         *
         * - create a new learner message
         * - generate an Emil reply
         * - run ordinary assessment
         * - evaluate a mission objective
         * - generate reply suggestions
         * - generate another Besser challenge
         */
        val activeRefinement =
            currentState
                .activeBesserRefinement

        if (activeRefinement != null) {
            submitBesserRefinement(
                scope =
                    scope,

                refinement =
                    activeRefinement,

                refinedMessageContent =
                    messageText,

                uiState =
                    uiState
            )

            return
        }

        /*
         * ===================================================
         * NORMAL CHAT SEND
         * ===================================================
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

    /*
     * =======================================================
     * BESSER REFINEMENT SUBMISSION
     * =======================================================
     */

    private fun submitBesserRefinement(
        scope: CoroutineScope,
        refinement: BesserRefinementUiState,
        refinedMessageContent: String,
        uiState:
        MutableStateFlow<ChatUiState>
    ) {
        val normalizedRefinement =
            refinedMessageContent.trim()

        if (
            normalizedRefinement.isBlank() ||
            uiState
                .value
                .isSubmittingBesserRefinement
        ) {
            return
        }

        uiState.update { state ->
            state
                .besserRefinementSubmissionStarted()
        }

        scope.launch {
            try {
                /*
                 * Read the latest UI challenge so hints revealed
                 * after refinement started are included in the
                 * assistance calculation.
                 */
                val currentChallenge =
                    uiState
                        .value
                        .besserChallenges
                        .values
                        .firstOrNull { challenge ->
                            challenge.id ==
                                    refinement.challengeId
                        }

                val revealedHintCount =
                    currentChallenge
                        ?.revealedHintCount
                        ?: refinement
                            .revealedHintCount

                val assistanceLevel =
                    determineBesserAssistanceLevel(
                        revealedHintCount =
                            revealedHintCount
                    )

                val result =
                    completeBesserChallengeUseCase(
                        challengeId =
                            refinement.challengeId,

                        refinedMessageContent =
                            normalizedRefinement,

                        actualAssistanceLevel =
                            assistanceLevel
                    )

                result
                    .onSuccess {
                        uiState.update { state ->
                            state
                                .besserRefinementSubmissionSucceeded()
                        }

                        Log.i(
                            CONTROLLER_TAG,
                            buildString {
                                append(
                                    "Besser refinement completed. "
                                )

                                append(
                                    "challengeId="
                                )

                                append(
                                    refinement.challengeId
                                )

                                append(
                                    ", capabilityId="
                                )

                                append(
                                    refinement.capabilityId
                                )

                                append(
                                    ", assistance="
                                )

                                append(
                                    assistanceLevel.name
                                )

                                append(
                                    ", revealedHintCount="
                                )

                                append(
                                    revealedHintCount
                                )
                            }
                        )
                    }
                    .onFailure { throwable: Throwable ->
                        Log.w(
                            CONTROLLER_TAG,
                            "Besser refinement was rejected.",
                            throwable
                        )

                        uiState.update { state ->
                            state.besserRefinementSubmissionFailed(
                                message =
                                    throwable.message
                                        ?: BESSER_REFINEMENT_ERROR
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
                    CONTROLLER_TAG,
                    "Besser refinement submission failed.",
                    throwable
                )

                uiState.update { state ->
                    state.besserRefinementSubmissionFailed(
                        message =
                            throwable.message
                                ?: BESSER_REFINEMENT_ERROR
                    )
                }
            }
        }
    }

    /*
     * Besser itself is already an explicit prompt, so a
     * successful Besser rewrite is never considered fully
     * INDEPENDENT evidence.
     *
     * No additional hint:
     *     PROMPTED
     *
     * One revealed hint:
     *     SCAFFOLDED
     *
     * Two or more revealed hints:
     *     MODELED
     */
    private fun determineBesserAssistanceLevel(
        revealedHintCount: Int
    ): AssistanceLevel {
        return when {
            revealedHintCount <=
                    0 -> {
                AssistanceLevel.PROMPTED
            }

            revealedHintCount ==
                    1 -> {
                AssistanceLevel.SCAFFOLDED
            }

            else -> {
                AssistanceLevel.MODELED
            }
        }
    }

    /*
     * =======================================================
     * NORMAL MESSAGE SEND
     * =======================================================
     */

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
            val activeMissionAtSendStart =
                runtime.activeMission

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
                            completedMissionAnalytics
                                .missionId
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
                            completedMissionAnalytics
                                .overallScore
                        )
                    }
                )
            }

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
                        !result.warning
                            .isNullOrBlank()
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

        const val BESSER_REFINEMENT_ERROR =
            "Die Verbesserung konnte nicht gespeichert werden."
    }
}