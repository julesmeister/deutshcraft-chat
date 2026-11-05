// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/ChatUiState.kt

package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.besser.presentation.BesserChallengeUiModel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestionAction
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationMode
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationUiState

data class ChatUiState(
    val messages: List<ChatMessage> =
        emptyList(),

    val messageInput: TextFieldValue =
        TextFieldValue(),

    /*
     * Contains only ordinary assistant-generated suggestions
     * and explicit completed-mission continuation actions.
     *
     * The active-mission branch action is derived through
     * visibleSuggestions so it cannot be overwritten by
     * progressive assistant suggestion updates.
     */
    val suggestions: List<ReplySuggestion> =
        emptyList(),

    val missionVariation:
    MissionVariationUiState =
        MissionVariationUiState(),

    val besserChallenges:
    Map<String, BesserChallengeUiModel> =
        emptyMap(),

    val activeBesserRefinement:
    BesserRefinementUiState? =
        null,

    val isSubmittingBesserRefinement: Boolean =
        false,

    val missionOffer: MissionOpportunity? =
        null,

    val isCheckingMissionOpportunity: Boolean =
        false,

    val isStartingMission: Boolean =
        false,

    val activeMissionId: String? =
        null,

    val activeMissionTitle: String? =
        null,

    val currentMissionObjective: String? =
        null,

    val currentMissionObjectiveDescription: String? =
        null,

    val currentMissionGuidance: String? =
        null,

    val missionProgressPercentage: Int =
        0,

    val objectiveCompletionEvent:
    MissionObjectiveCompletionEvent? =
        null,

    val adaptiveLearningFeedbackEvent:
    AdaptiveLearningFeedbackEvent? =
        null,

    val isInitializing: Boolean =
        true,

    val isSending: Boolean =
        false,

    val isAssistantTyping: Boolean =
        false,

    val isAssessing: Boolean =
        false,

    val errorMessage: String? =
        null,

    val error: ChatErrorType? =
        null,

    val currentStreak: Int =
        0
) {

    val isBesserRefinementMode: Boolean
        get() =
            activeBesserRefinement !=
                    null

    val besserRefinementInstruction: String?
        get() =
            activeBesserRefinement
                ?.instruction

    val hasActiveMission: Boolean
        get() =
            activeMissionId !=
                    null

    val isMissionVariationBusy: Boolean
        get() =
            isStartingMission ||
                    missionVariation.isStarting

    /*
     * Indicates that the current state already represents a
     * completed-mission continuation.
     *
     * In that case the state contains explicit suggestions such
     * as:
     *
     * - Noch eine Runde?
     * - Für jetzt fertig
     *
     * The active-mission branch suggestion must not be added on
     * top of those continuation actions.
     */
    val hasCompletedMissionContinuation: Boolean
        get() =
            missionVariation
                .context
                ?.mode ==
                    MissionVariationMode
                        .EXTEND_COMPLETED_MISSION

    /*
     * The active-mission branch action is a presentation-level
     * suggestion.
     *
     * It is not stored in ReplySuggestionCache and therefore
     * cannot be removed when progressive Qwen suggestions
     * replace the ordinary suggestion list.
     */
    val shouldShowActiveMissionBranchSuggestion: Boolean
        get() =
            hasActiveMission &&
                    !hasCompletedMissionContinuation &&
                    !isBesserRefinementMode &&
                    !isInitializing &&
                    !isMissionVariationBusy

    /*
     * Final suggestion list rendered by ChatScreen.
     *
     * The action is kept last so ordinary conversational replies
     * remain the primary choices.
     */
    val visibleSuggestions: List<ReplySuggestion>
        get() {
            val normalizedSuggestions =
                suggestions
                    .filterNot { suggestion ->
                        suggestion.id ==
                                ChatPresentationTokens.UiState.ActiveMissionBranchSuggestionId
                    }
                    .distinctBy { suggestion ->
                        suggestion.id
                    }

            if (
                !shouldShowActiveMissionBranchSuggestion
            ) {
                return normalizedSuggestions
            }

            return (
                    normalizedSuggestions +
                            activeMissionBranchSuggestion
                    )
                .takeLast(
                    ChatPresentationTokens.UiState.MaximumVisibleSuggestionCount
                )
        }

    val hasVisibleSuggestionContent: Boolean
        get() =
            missionOffer !=
                    null ||
                    visibleSuggestions.isNotEmpty()

    private val activeMissionBranchSuggestion:
            ReplySuggestion
        get() =
            ReplySuggestion(
                id =
                    ChatPresentationTokens.UiState.ActiveMissionBranchSuggestionId,

                text =
                    ChatPresentationTokens.UiState.ActiveMissionBranchSuggestionText,

                action =
                    ReplySuggestionAction
                        .OpenMissionVariations
            )

}
