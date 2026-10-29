package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.besser.presentation.BesserChallengeUiModel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity

data class ChatUiState(
    val messages: List<ChatMessage> =
        emptyList(),

    val messageInput: TextFieldValue =
        TextFieldValue(),

    val suggestions: List<ReplySuggestion> =
        emptyList(),

    /*
     * =======================================================
     * BESSER
     * =======================================================
     */

    val besserChallenges:
    Map<String, BesserChallengeUiModel> =
        emptyMap(),

    /*
     * Non-null while the learner is actively rewriting a
     * sentence for an accepted Besser challenge.
     *
     * Refinement submission is intentionally different from
     * ordinary chat sending.
     */
    val activeBesserRefinement:
    BesserRefinementUiState? =
        null,

    val isSubmittingBesserRefinement: Boolean =
        false,

    /*
     * =======================================================
     * MISSION OFFER
     * =======================================================
     */

    val missionOffer: MissionOpportunity? =
        null,

    val isCheckingMissionOpportunity: Boolean =
        false,

    val isStartingMission: Boolean =
        false,

    /*
     * =======================================================
     * ACTIVE MISSION
     * =======================================================
     */

    val activeMissionId: String? =
        null,

    val activeMissionTitle: String? =
        null,

    val currentMissionObjective: String? =
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

    /*
     * =======================================================
     * RUNTIME PRESENTATION STATE
     * =======================================================
     */

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
}