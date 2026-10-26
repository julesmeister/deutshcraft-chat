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
     * Accepts the currently displayed AI-generated mission.
     */
    data object StartOfferedMission :
        ChatUiAction

    /*
     * Removes the current mission offer and prevents the
     * same generated mission title from being offered again
     * in this conversation during the current process.
     */
    data object DismissMissionOffer :
        ChatUiAction

    data class ObjectiveCompletionShown(
        val eventId: String
    ) : ChatUiAction

    data object ErrorDismissed :
        ChatUiAction
}