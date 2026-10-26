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

    data class ObjectiveCompletionShown(
        val eventId: String
    ) : ChatUiAction

    data object ErrorDismissed :
        ChatUiAction
}