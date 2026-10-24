package com.julesmeister.deutschcraft.feature.chat.presentation

sealed interface ChatUiAction {

    /**
     * Triggered whenever the user changes the
     * message composer text.
     */
    data class MessageInputChanged(
        val value: String
    ) : ChatUiAction

    /**
     * Triggered by the send button or keyboard
     * IME send action.
     */
    data object SendMessage : ChatUiAction

    /**
     * Triggered when the user selects one of the
     * generated reply suggestions.
     *
     * The selected suggestion fills the composer,
     * allowing the user to edit it before sending.
     */
    data class SuggestionSelected(
        val suggestionId: String
    ) : ChatUiAction

    /**
     * Clears an error after ChatScreen displays it
     * through the Snackbar.
     */
    data object ErrorDismissed : ChatUiAction
}