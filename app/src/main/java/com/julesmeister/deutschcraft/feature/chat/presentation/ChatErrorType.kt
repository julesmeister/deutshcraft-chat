package com.julesmeister.deutschcraft.feature.chat.presentation

sealed class ChatErrorType {
    data class AuthError(val message: String = "Authentication failed") :
        ChatErrorType()

    data class NetworkError(val message: String = "Network error") :
        ChatErrorType()

    data class SyncError(val message: String = "Sync failed") :
        ChatErrorType()

    data class GenericError(val message: String) :
        ChatErrorType()

    val displayMessage: String
        get() = when (this) {
            is AuthError -> message
            is NetworkError -> message
            is SyncError -> message
            is GenericError -> message
        }
}
