package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.ui.text.input.TextFieldValue

sealed interface ChatUiAction {

    data class MessageInputChanged(
        val value: TextFieldValue
    ) : ChatUiAction

    data object SendMessage :
        ChatUiAction

    data class SuggestionSelected(
        val suggestionId: String
    ) : ChatUiAction

    /*
     * Leaves Besser refinement mode without skipping the
     * underlying challenge.
     *
     * The accepted challenge remains available and can be
     * reopened with Verbessern.
     */
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