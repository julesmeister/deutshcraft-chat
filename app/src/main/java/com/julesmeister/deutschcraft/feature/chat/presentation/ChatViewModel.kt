package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.julesmeister.deutschcraft.app.navigation.ChatDestination
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
    MarkedPhraseController
) : ViewModel() {

    /*
     * Conversation opened by navigation.
     *
     * Kept as a property because marked phrases need it
     * whenever the learner marks a new phrase.
     */
    private val conversationId: String =
        savedStateHandle
            .toRoute<ChatDestination>()
            .conversationId

    /*
     * Main conversation state.
     */
    val uiState:
            StateFlow<ChatUiState> =
        coordinator.uiState

    /*
     * Finalized mission-result state.
     */
    val missionResultUiState:
            StateFlow<MissionResultUiState> =
        missionResultController.uiState

    /*
     * Learner-facing support for the currently active mission.
     */
    val missionGuidanceUiState:
            StateFlow<MissionGuidanceUiState> =
        missionGuidanceController.uiState

    /*
     * messageId -> phrases the learner marked on that message.
     *
     * ChatMessageList attaches these as explanation bubbles
     * under the original message.
     */
    val markedPhrases:
            StateFlow<Map<String, List<MarkedPhrase>>> =
        markedPhraseController.phrasesByMessage

    init {
        /*
         * Give MissionGuidanceController the lifecycle-aware
         * scope required for:
         *
         * - restoring persisted hints
         * - recording newly revealed hints
         */
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

        /*
         * Observe marked phrases for this conversation from
         * Room.
         */
        markedPhraseController.start(
            conversationId =
                conversationId,
            scope =
                viewModelScope
        )
    }

    /*
     * Ordinary chat actions remain delegated to the session
     * coordinator.
     */
    fun onAction(
        action:
        ChatUiAction
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
     * MARKED PHRASES
     * =======================================================
     */

    /*
     * Saves the phrase immediately (bubble appears as
     * pending), then fetches the explanation in the
     * background.
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

    /*
     * For now Continue simply returns to the existing
     * conversation.
     *
     * Later this can also trigger:
     *
     * - CEFR progress refresh
     * - recommendation refresh
     */
    fun continueAfterMissionResult() {
        missionResultController.dismiss()
    }

    override fun onCleared() {
        coordinator.stop()

        /*
         * Stop observing marked phrases for this conversation.
         */
        markedPhraseController.stop()

        /*
         * Cancel guidance coroutines managed through this
         * ViewModel's lifetime.
         */
        missionGuidanceController.cancel()

        /*
         * Do not leave a visible result from this conversation
         * after its ViewModel is destroyed.
         */
        missionResultController.clear()

        super.onCleared()
    }
}