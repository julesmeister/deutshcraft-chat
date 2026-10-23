package com.julesmeister.deutschcraft.feature.chat.presentation

sealed interface ChatUiAction {

    data class MessageInputChanged(
        val value: String
    ) : ChatUiAction

    data object SendMessage : ChatUiAction

    data class SuggestionSelected(
        val suggestionId: String
    ) : ChatUiAction

    data object ProgressClicked : ChatUiAction

    data object RetryMessage : ChatUiAction

    data object ErrorDismissed : ChatUiAction
}