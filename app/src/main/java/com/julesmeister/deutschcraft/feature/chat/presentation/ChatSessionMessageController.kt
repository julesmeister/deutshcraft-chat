// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/ChatSessionMessageController.kt

package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.besser.domain.AssistanceLevel
import com.julesmeister.deutschcraft.feature.besser.domain.BesserRefinementEvaluation
import com.julesmeister.deutschcraft.feature.besser.domain.RefinementVerdict
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.CompleteBesserChallengeUseCase
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.EvaluateBesserRefinementUseCase
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
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
    CompleteBesserChallengeUseCase,

    private val evaluateBesserRefinementUseCase:
    EvaluateBesserRefinementUseCase
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

        if (currentState.isBesserRefinementMode) {
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
            runtime.isSending
        ) {
            return
        }

        val activeRefinement =
            currentState.activeBesserRefinement

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

        runtime.cancelSuggestionJob()

        messageSender.clearSuggestions(
            conversationId =
                conversationId
        )

        uiState.update { state ->
            state.sendingStarted()
        }

        runtime.sendingJob =
            scope.launch {
                sendCriticalConversationTurn(
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

    private suspend fun sendCriticalConversationTurn(
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
        val activeMissionAtSendStart =
            runtime.activeMission

        try {
            val immediateResult =
                messageSender.sendCritical(
                    conversationId =
                        conversationId,

                    messageText =
                        messageText,

                    activeMission =
                        activeMissionAtSendStart
                )

            runtime.sendingJob =
                null

            uiState.update { state ->
                state.assistantReplyPersisted(
                    initialSuggestions =
                        immediateResult.firstSuggestions
                )
            }

            Log.d(
                CONTROLLER_TAG,
                buildString {
                    append(
                        "Assistant reply persisted. "
                    )

                    append(
                        "assistantMessageId="
                    )

                    append(
                        immediateResult.assistantMessageId
                    )

                    append(
                        ", composerUnlocked=true"
                    )

                    append(
                        ", firstSuggestionCount="
                    )

                    append(
                        immediateResult
                            .firstSuggestions
                            .size
                    )
                }
            )

            runtime.cancelSuggestionJob()

            runtime.suggestionJob =
                messageSender
                    .startSuggestionPresentation(
                        scope =
                            scope,

                        conversationId =
                            conversationId,

                        assistantMessageId =
                            immediateResult
                                .assistantMessageId,

                        remainingSuggestions =
                            immediateResult
                                .remainingSuggestions,

                        onSuggestionsUpdated = {
                                assistantMessageId,
                                suggestions ->

                            val latestAssistantMessageId =
                                uiState
                                    .value
                                    .messages
                                    .lastOrNull { message ->
                                        message.sender ==
                                                MessageSender.ASSISTANT
                                    }
                                    ?.id

                            if (
                                latestAssistantMessageId ==
                                null ||
                                latestAssistantMessageId ==
                                assistantMessageId
                            ) {
                                uiState.update { state ->
                                    state.withProgressiveSuggestions(
                                        suggestions =
                                            suggestions
                                    )
                                }
                            }
                        }
                    )

            runtime.cancelPostSendLearningJob()

            runtime.postSendLearningJob =
                messageSender
                    .startPostSendLearning(
                        scope =
                            scope,

                        conversationId =
                            conversationId,

                        submittedMessageText =
                            messageText,

                        activeMission =
                            activeMissionAtSendStart,

                        suggestions =
                            immediateResult
                                .finalSuggestions,

                        warning =
                            immediateResult.warning,

                        onCompleted = { result ->
                            val completedMissionTitle =
                                result.completedMissionTitle

                            val completedMissionAnalytics =
                                result.completedMissionAnalytics

                            if (
                                !completedMissionTitle
                                    .isNullOrBlank() &&
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
                                            "Completed mission forwarded "
                                        )

                                        append(
                                            "to presentation. missionId="
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

                            if (
                                !result.warning.isNullOrBlank()
                            ) {
                                uiState.update { state ->
                                    state.copy(
                                        errorMessage =
                                            result.warning
                                    )
                                }
                            }
                        },

                        onFailure = { throwable ->
                            Log.e(
                                CONTROLLER_TAG,
                                "Supplementary post-send learning failed.",
                                throwable
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
                "Critical message sending failed.",
                throwable
            )

            runtime.sendingJob =
                null

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
        }
    }

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

        if (
            normalizedRefinement ==
            refinement
                .originalMessageContent
                .trim()
        ) {
            uiState.update { state ->
                state.besserRefinementNeedsRevision(
                    feedback =
                        UNCHANGED_FEEDBACK,

                    score =
                        null
                )
            }

            return
        }

        uiState.update { state ->
            state.besserRefinementSubmissionStarted()
        }

        scope.launch {
            try {
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
                        ?: refinement.revealedHintCount

                val canRevealHint =
                    currentChallenge
                        ?.canRevealHint
                        ?: false

                val evaluation =
                    evaluateBesserRefinementUseCase(
                        challengeId =
                            refinement.challengeId,

                        refinedText =
                            normalizedRefinement
                    )
                        .onFailure { throwable ->
                            Log.w(
                                CONTROLLER_TAG,
                                "Besser evaluation unavailable. " +
                                        "Completing without evaluation.",
                                throwable
                            )
                        }
                        .getOrNull()

                if (evaluation != null) {
                    Log.i(
                        CONTROLLER_TAG,
                        buildString {
                            append(
                                "Besser evaluation. "
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
                                ", score="
                            )

                            append(
                                evaluation.score
                            )

                            append(
                                ", verdict="
                            )

                            append(
                                evaluation.verdict
                            )

                            append(
                                ", evidence="
                            )

                            append(
                                evaluation.evidence
                            )
                        }
                    )
                }

                when (evaluation?.verdict) {
                    RefinementVerdict.PARTIAL -> {
                        uiState.update { state ->
                            state.besserRefinementNeedsRevision(
                                feedback =
                                    evaluation.feedback,

                                score =
                                    evaluation.score
                            )
                        }
                    }

                    RefinementVerdict.NOT_DEMONSTRATED -> {
                        val feedback =
                            if (canRevealHint) {
                                evaluation.feedback +
                                        HINT_SUGGESTION_SUFFIX
                            } else {
                                evaluation.feedback
                            }

                        uiState.update { state ->
                            state.besserRefinementNeedsRevision(
                                feedback =
                                    feedback,

                                score =
                                    evaluation.score
                            )
                        }
                    }

                    RefinementVerdict.PASSED -> {
                        completeRefinement(
                            refinement =
                                refinement,

                            normalizedRefinement =
                                normalizedRefinement,

                            revealedHintCount =
                                revealedHintCount,

                            evaluation =
                                evaluation,

                            uiState =
                                uiState
                        )
                    }

                    null -> {
                        completeRefinement(
                            refinement =
                                refinement,

                            normalizedRefinement =
                                normalizedRefinement,

                            revealedHintCount =
                                revealedHintCount,

                            evaluation =
                                null,

                            uiState =
                                uiState
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

    private suspend fun completeRefinement(
        refinement: BesserRefinementUiState,
        normalizedRefinement: String,
        revealedHintCount: Int,
        evaluation: BesserRefinementEvaluation?,
        uiState:
        MutableStateFlow<ChatUiState>
    ) {
        val assistanceLevel =
            determineBesserAssistanceLevel(
                revealedHintCount =
                    revealedHintCount
            )

        completeBesserChallengeUseCase(
            challengeId =
                refinement.challengeId,

            refinedMessageContent =
                normalizedRefinement,

            actualAssistanceLevel =
                assistanceLevel,

            evaluationScore =
                evaluation?.score,

            evaluationFeedback =
                evaluation?.feedback,

            evaluationEvidence =
                evaluation?.evidence
        )
            .onSuccess {
                uiState.update { state ->
                    state.besserRefinementSubmissionSucceeded()
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

                        append(
                            ", evaluationScore="
                        )

                        append(
                            evaluation?.score
                        )

                        append(
                            ", evaluationPersisted="
                        )

                        append(
                            evaluation !=
                                    null
                        )
                    }
                )
            }
            .onFailure { throwable ->
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
    }

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

        const val UNCHANGED_FEEDBACK =
            "Ändere deinen Satz, damit er die Aufgabe erfüllt."

        const val HINT_SUGGESTION_SUFFIX =
            " Tippe auf „Tipp“, wenn du Hilfe brauchst."
    }
}