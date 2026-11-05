package com.julesmeister.deutschcraft.feature.chat.presentation

import com.julesmeister.deutschcraft.feature.chat.data.ai.AiGatewayException
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
import java.io.IOException
import retrofit2.HttpException

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

internal fun Throwable.toChatError(): ChatErrorType {
    val visited = mutableSetOf<Throwable>()
    val causes = generateSequence(this) { it.cause }.takeWhile(visited::add).toList()
    val httpError = causes.filterIsInstance<HttpException>().firstOrNull()
    if (httpError != null) {
        val code = httpError.code()
        return when (code) {
            401 -> ChatErrorType.AuthError("Session expired (401). Please sign in again.")
            403 -> ChatErrorType.AuthError("Access denied (403). Check your sign-in or permissions.")
            429 -> ChatErrorType.NetworkError("Too many requests (429). Please try again later.")
            in 500..599 -> ChatErrorType.NetworkError("AI service unavailable ($code). Try again later.")
            else -> ChatErrorType.NetworkError("Request failed (HTTP $code). Please try again.")
        }
    }
    if (causes.any { it is IOException }) {
        return ChatErrorType.NetworkError("Cannot reach AI service. Check your connection.")
    }
    return ChatErrorType.GenericError(
        causes.filterIsInstance<AiGatewayException>().firstOrNull()?.message
            ?: ChatPresentationTokens.SessionMessageController.SendError
    )
}
