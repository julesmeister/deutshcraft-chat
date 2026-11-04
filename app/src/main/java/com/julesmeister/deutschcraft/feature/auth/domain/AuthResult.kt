package com.julesmeister.deutschcraft.feature.auth.domain

sealed interface AuthResult<out T> {

    data class Success<T>(
        val value: T
    ) : AuthResult<T>

    data class Failure(
        val error: AuthError
    ) : AuthResult<Nothing>
}

sealed interface AuthError {

    data object NotConfigured : AuthError

    data object Network : AuthError

    data object InvalidCredentials : AuthError

    data object UserNotConfirmed : AuthError

    data object UserAlreadyExists : AuthError

    data object UserNotFound : AuthError

    data class InvalidPassword(
        val detail: String?
    ) : AuthError

    data object InvalidCode : AuthError

    data object ExpiredCode : AuthError

    data object TooManyAttempts : AuthError

    data object SessionExpired : AuthError

    data object GoogleCancelled : AuthError

    data class Unknown(
        val detail: String?
    ) : AuthError
}

/*
 * Outcome of SignUp. Cognito auto-confirms only when a
 * pre-sign-up Lambda says so; normally an email code is sent.
 */
data class SignUpOutcome(
    val needsConfirmation: Boolean,
    val codeDestination: String?
)
