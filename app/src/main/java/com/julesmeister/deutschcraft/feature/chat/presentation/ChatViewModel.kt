package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.julesmeister.deutschcraft.app.navigation.ChatDestination
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.AcceptBesserChallengeUseCase
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.RevealNextBesserHintUseCase
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.SkipBesserChallengeUseCase
import com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result.MissionResultController
import com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result.MissionResultUiState
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceController
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceUiState
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhrase
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhraseType
import com.julesmeister.deutschcraft.feature.phrasemarking.presentation.MarkedPhraseController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel
@Inject constructor(
    savedStateHandle:
    SavedStateHandle,

    private val coordinator:
    ChatSessionCoordinator,

    private val missionResultController:
    MissionResultController,

    private val missionGuidanceController:
    MissionGuidanceController,

    private val markedPhraseController:
    MarkedPhraseController,

    /*
     * =======================================================
     * BESSER
     * =======================================================
     */

    private val acceptBesserChallengeUseCase:
    AcceptBesserChallengeUseCase,

    private val revealNextBesserHintUseCase:
    RevealNextBesserHintUseCase,

    private val skipBesserChallengeUseCase:
    SkipBesserChallengeUseCase
) : ViewModel() {

    /*
     * =======================================================
     * CONVERSATION
     * =======================================================
     */

    private val conversationId: String =
        savedStateHandle
            .toRoute<ChatDestination>()
            .conversationId

    /*
     * =======================================================
     * MAIN CHAT STATE
     * =======================================================
     */

    val uiState:
            StateFlow<ChatUiState> =
        coordinator.uiState

    /*
     * =======================================================
     * MISSION RESULT
     * =======================================================
     */

    val missionResultUiState:
            StateFlow<MissionResultUiState> =
        missionResultController.uiState

    /*
     * =======================================================
     * MISSION GUIDANCE
     * =======================================================
     */

    val missionGuidanceUiState:
            StateFlow<MissionGuidanceUiState> =
        missionGuidanceController.uiState

    /*
     * =======================================================
     * MARKED PHRASES
     * =======================================================
     *
     * messageId -> marked phrases belonging to that message.
     */

    val markedPhrases:
            StateFlow<Map<String, List<MarkedPhrase>>> =
        markedPhraseController.phrasesByMessage

    init {
        /*
         * Mission guidance uses this lifecycle-aware scope for
         * restoring and persisting learner support state.
         */
        missionGuidanceController.attachScope(
            scope =
                viewModelScope
        )

        /*
         * Start the main conversation session.
         */
        coordinator.start(
            conversationId =
                conversationId,
            scope =
                viewModelScope
        )

        /*
         * Observe marked phrases for this conversation.
         */
        markedPhraseController.start(
            conversationId =
                conversationId,
            scope =
                viewModelScope
        )
    }

    /*
     * =======================================================
     * CHAT
     * =======================================================
     */

    fun onAction(
        action: ChatUiAction
    ) {
        coordinator.onAction(
            action =
                action,
            scope =
                viewModelScope
        )
    }

    /*
     * =======================================================
     * BESSER
     * =======================================================
     */

    /*
     * Accept the challenge and move the original learner
     * sentence into the composer.
     *
     * The actual refinement-submission flow is handled
     * separately from this initial transition.
     */
    fun improveBesserChallenge(
        challengeId: String
    ) {
        if (
            challengeId.isBlank()
        ) {
            return
        }

        /*
         * Resolve the challenge while it is still AVAILABLE.
         *
         * The challenge remains observable after ACCEPTED, but
         * resolving it here makes this flow independent of any
         * future observer filtering changes.
         */
        val challenge =
            uiState
                .value
                .besserChallenges
                .values
                .firstOrNull { challenge ->
                    challenge.id ==
                            challengeId
                }
                ?: run {
                    coordinator.showBesserError(
                        message =
                            "The Besser challenge is no longer available."
                    )

                    return
                }

        /*
         * The owning source message must still exist in the
         * conversation because its original text will populate
         * the composer.
         */
        val sourceMessageExists =
            uiState
                .value
                .messages
                .any { message ->
                    message.id ==
                            challenge.messageId
                }

        if (!sourceMessageExists) {
            coordinator.showBesserError(
                message =
                    "The message for this Besser challenge could not be found."
            )

            return
        }

        viewModelScope.launch {
            val result =
                acceptBesserChallengeUseCase(
                    challengeId =
                        challengeId
                )

            result
                .onSuccess {
                    val started =
                        coordinator.startBesserRefinement(
                            challengeId =
                                challengeId
                        )

                    if (!started) {
                        coordinator.showBesserError(
                            message =
                                "DeutschCraft could not start the Besser refinement."
                        )
                    }
                }
                .onFailure { throwable: Throwable ->
                    coordinator.showBesserError(
                        message =
                            throwable.message
                                ?: "The Besser challenge could not be started."
                    )
                }
        }
    }

    /*
     * Reveal one additional hint.
     *
     * Room remains the source of truth. Once
     * revealedHintCount changes, the conversation Besser
     * observer emits an updated BesserChallengeUiModel and the
     * bubble expands automatically.
     */
    fun revealNextBesserHint(
        challengeId: String
    ) {
        if (
            challengeId.isBlank()
        ) {
            return
        }

        viewModelScope.launch {
            val result =
                revealNextBesserHintUseCase(
                    challengeId =
                        challengeId
                )

            result.onFailure { throwable: Throwable ->
                coordinator.showBesserError(
                    message =
                        throwable.message
                            ?: "The next Besser hint could not be revealed."
                )
            }
        }
    }

    /*
     * Skip an AVAILABLE or ACCEPTED challenge.
     *
     * Once persisted as SKIPPED, the active challenge observer
     * stops returning it and the bubble disappears
     * automatically.
     */
    fun skipBesserChallenge(
        challengeId: String
    ) {
        if (
            challengeId.isBlank()
        ) {
            return
        }

        viewModelScope.launch {
            val result =
                skipBesserChallengeUseCase(
                    challengeId =
                        challengeId
                )

            result.onFailure { throwable: Throwable ->
                coordinator.showBesserError(
                    message =
                        throwable.message
                            ?: "The Besser challenge could not be skipped."
                )
            }
        }
    }

    /*
     * =======================================================
     * MARKED PHRASES
     * =======================================================
     */

    fun markPhrase(
        messageId: String,
        phrase: String,
        sourceSentence: String,
        type: MarkedPhraseType
    ) {
        markedPhraseController.mark(
            conversationId =
                conversationId,

            messageId =
                messageId,

            phrase =
                phrase,

            sourceSentence =
                sourceSentence,

            type =
                type,

            scope =
                viewModelScope
        )
    }

    fun removeMarkedPhrase(
        id: String
    ) {
        markedPhraseController.remove(
            id =
                id,

            scope =
                viewModelScope
        )
    }

    /*
     * =======================================================
     * MISSION GUIDANCE
     * =======================================================
     */

    fun openMissionGuidance() {
        missionGuidanceController.show()
    }

    fun dismissMissionGuidance() {
        missionGuidanceController.dismiss()
    }

    fun revealNextMissionHint() {
        missionGuidanceController.revealNextHint()
    }

    fun toggleMissionVocabulary() {
        missionGuidanceController.toggleVocabulary()
    }

    fun toggleMissionExamples() {
        missionGuidanceController.toggleExamples()
    }

    /*
     * =======================================================
     * MISSION RESULT
     * =======================================================
     */

    fun dismissMissionResult() {
        missionResultController.dismiss()
    }

    fun continueAfterMissionResult() {
        missionResultController.dismiss()
    }

    /*
     * =======================================================
     * CLEANUP
     * =======================================================
     */

    override fun onCleared() {
        coordinator.stop()

        markedPhraseController.stop()

        missionGuidanceController.cancel()

        missionResultController.clear()

        super.onCleared()
    }
}