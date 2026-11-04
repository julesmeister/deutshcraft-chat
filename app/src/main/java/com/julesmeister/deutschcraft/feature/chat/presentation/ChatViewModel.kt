// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/ChatViewModel.kt

package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.julesmeister.deutschcraft.app.navigation.ChatDestination
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.auth.domain.AuthRepository
import com.julesmeister.deutschcraft.feature.auth.domain.GuestMessagePolicy
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.AcceptBesserChallengeUseCase
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.RevealNextBesserHintUseCase
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.SkipBesserChallengeUseCase
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
import com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result.MissionResultController
import com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result.MissionResultUiState
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationContext
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceController
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceUiState
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhrase
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhraseType
import com.julesmeister.deutschcraft.feature.phrasemarking.presentation.MarkedPhraseController
import com.julesmeister.deutschcraft.feature.settings.data.SettingsRepository
import com.julesmeister.deutschcraft.feature.settings.domain.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    private val settingsRepository:
    SettingsRepository,

    private val acceptBesserChallengeUseCase:
    AcceptBesserChallengeUseCase,

    private val revealNextBesserHintUseCase:
    RevealNextBesserHintUseCase,

    private val skipBesserChallengeUseCase:
    SkipBesserChallengeUseCase,

    authRepository:
    AuthRepository
) : ViewModel() {

    private val conversationId: String =
        savedStateHandle
            .toRoute<ChatDestination>()
            .conversationId

    val uiState: StateFlow<ChatUiState> =
        coordinator.uiState

    val settings: Flow<UserSettings> =
        settingsRepository.settings

    val missionResultUiState:
            StateFlow<MissionResultUiState> =
        missionResultController.uiState

    val missionGuidanceUiState:
            StateFlow<MissionGuidanceUiState> =
        missionGuidanceController.uiState

    val markedPhrases:
            StateFlow<Map<String, List<MarkedPhrase>>> =
        markedPhraseController.phrasesByMessage

    val guestGate: StateFlow<ChatGuestGate> =
        combine(
            uiState
                .map { state ->
                    state.messages.count { message ->
                        message.sender == MessageSender.USER
                    }
                }
                .distinctUntilChanged(),
            authRepository.session
        ) { sentUserMessages, session ->
            ChatGuestGate(
                remaining =
                    GuestMessagePolicy.remainingMessages(
                        session = session,
                        sentUserMessages = sentUserMessages
                    )
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = ChatGuestGate()
            )

    init {
        missionGuidanceController.attachScope(
            scope =
                viewModelScope
        )

        coordinator.start(
            conversationId =
                conversationId,

            scope =
                viewModelScope
        )

        markedPhraseController.start(
            conversationId =
                conversationId,

            scope =
                viewModelScope
        )
    }

    fun onAction(
        action: ChatUiAction
    ) {
        if (guestGate.value.isLocked && action.requiresAiCall()) {
            return
        }

        coordinator.onAction(
            action =
                action,

            scope =
                viewModelScope
        )
    }

    fun improveBesserChallenge(
        challengeId: String
    ) {
        if (challengeId.isBlank()) {
            return
        }

        val challenge =
            uiState
                .value
                .besserChallenges
                .values
                .firstOrNull { candidate ->
                    candidate.id ==
                            challengeId
                }
                ?: run {
                    coordinator.showBesserError(
                        message =
                            "The Besser challenge is no longer available."
                    )

                    return
                }

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
            acceptBesserChallengeUseCase(
                challengeId =
                    challengeId
            )
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
                .onFailure { throwable ->
                    coordinator.showBesserError(
                        message =
                            throwable.message
                                ?: "The Besser challenge could not be started."
                    )
                }
        }
    }

    fun revealNextBesserHint(
        challengeId: String
    ) {
        if (challengeId.isBlank()) {
            return
        }

        viewModelScope.launch {
            revealNextBesserHintUseCase(
                challengeId =
                    challengeId
            )
                .onFailure { throwable ->
                    coordinator.showBesserError(
                        message =
                            throwable.message
                                ?: "The next Besser hint could not be revealed."
                    )
                }
        }
    }

    fun skipBesserChallenge(
        challengeId: String
    ) {
        if (challengeId.isBlank()) {
            return
        }

        viewModelScope.launch {
            skipBesserChallengeUseCase(
                challengeId =
                    challengeId
            )
                .onFailure { throwable ->
                    coordinator.showBesserError(
                        message =
                            throwable.message
                                ?: "The Besser challenge could not be skipped."
                    )
                }
        }
    }

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

    fun dismissMissionResult() {
        missionResultController.dismiss()
    }

    fun continueAfterMissionResult() {
        val result =
            missionResultUiState.value

        val missionId =
            result
                .missionId
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

        if (missionId != null) {
            coordinator.showMissionContinuation(
                context =
                    MissionVariationContext(
                        completedMissionId =
                            missionId,

                        missionTitle =
                            result
                                .missionTitle
                                .trim()
                                .ifBlank {
                                    ChatPresentationTokens.ViewModel.DefaultMissionTitle
                                },

                        cefrLevel =
                            result
                                .cefrLevelLabel
                                .toCefrLevel(),

                        overallScore =
                            result.safeOverallScore,

                        grammarScore =
                            result.grammarScore,

                        vocabularyScore =
                            result.vocabularyScore,

                        strongObjectiveTitles =
                            result.strongObjectiveTitles,

                        improvementObjectiveTitles =
                            result.improvementObjectiveTitles,

                        demonstratedVocabulary =
                            result.correctlyUsedTargetVocabulary,

                        unusedTargetVocabulary =
                            result.unusedTargetVocabulary
                    )
            )
        }

        missionResultController.dismiss()
    }

    override fun onCleared() {
        coordinator.stop()

        markedPhraseController.stop()

        missionGuidanceController.cancel()

        missionResultController.clear()

        super.onCleared()
    }

    private fun ChatUiAction.requiresAiCall(): Boolean =
        this == ChatUiAction.SendMessage ||
                this == ChatUiAction.StartOfferedMission ||
                this is ChatUiAction.StartMissionVariation

    private fun String.toCefrLevel():
            CefrLevel {

        return runCatching {
            CefrLevel.valueOf(
                trim().uppercase()
            )
        }
            .getOrDefault(
                CefrLevel.A1
            )
    }

}
