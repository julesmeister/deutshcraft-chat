// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/ChatUiAction.kt

package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationType

sealed interface ChatUiAction {

    data class MessageInputChanged(
        val value: TextFieldValue
    ) : ChatUiAction

    data object SendMessage :
        ChatUiAction

    data class SuggestionSelected(
        val suggestionId: String
    ) : ChatUiAction

    data object OpenMissionVariationPicker :
        ChatUiAction

    data object DismissMissionVariationPicker :
        ChatUiAction

    data object DismissMissionContinuation :
        ChatUiAction

    data class StartMissionVariation(
        val variationType: MissionVariationType
    ) : ChatUiAction

    data object CancelBesserRefinement :
        ChatUiAction

    data object StartOfferedMission :
        ChatUiAction

    data object DismissMissionOffer :
        ChatUiAction

    data class ObjectiveCompletionShown(
        val eventId: String
    ) : ChatUiAction

    data class AdaptiveLearningFeedbackShown(
        val eventId: String
    ) : ChatUiAction

    data object ErrorDismissed :
        ChatUiAction
}