package com.julesmeister.deutschcraft.feature.auth.presentation

import com.julesmeister.deutschcraft.feature.auth.domain.AuthError
import com.julesmeister.deutschcraft.feature.auth.presentation.theme.AuthDesignTokens

internal fun AuthError.toMessage(): String =
    when (this) {
        AuthError.NotConfigured ->
            "Sign-in isn't set up on this build yet."

        AuthError.Network ->
            "No connection. Check your internet and try again."

        AuthError.InvalidCredentials ->
            "Incorrect email or password."

        AuthError.UserNotConfirmed ->
            "Please verify your email first."

        AuthError.UserAlreadyExists ->
            "An account with this email already exists. Try signing in."

        AuthError.UserNotFound ->
            "We couldn't find an account with that email."

        is AuthError.InvalidPassword ->
            detail
                ?.substringAfterLast(": ")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let { "${it.trimEnd('.')}." }
                ?: "That password doesn't meet the requirements."

        AuthError.InvalidCode ->
            "That code isn't right. Check your email and try again."

        AuthError.ExpiredCode ->
            "That code has expired. Request a new one."

        AuthError.TooManyAttempts ->
            "Too many attempts. Please wait a moment and try again."

        AuthError.SessionExpired ->
            "Your session expired. Please sign in again."

        AuthError.GoogleCancelled ->
            "Google sign-in was cancelled."

        is AuthError.Unknown ->
            detail
                ?.takeIf { it.isNotBlank() }
                ?: "Something went wrong. Please try again."
    }

internal object AuthValidation {

    private val emailPattern =
        Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun emailError(
        email: String
    ): String? =
        when {
            email.isBlank() -> "Enter your email."
            !emailPattern.matches(email.trim()) -> "Enter a valid email address."
            else -> null
        }

    fun passwordError(
        password: String
    ): String? =
        when {
            password.isEmpty() -> "Enter a password."
            password.length < AuthDesignTokens.MinPasswordLength ->
                "Use at least ${AuthDesignTokens.MinPasswordLength} characters."
            else -> null
        }

    fun codeError(
        code: String
    ): String? =
        when {
            code.length != AuthDesignTokens.CodeLength ->
                "Enter the ${AuthDesignTokens.CodeLength}-digit code."
            else -> null
        }

    const val PasswordHint =
        "At least 8 characters with upper- and lowercase letters, a number and a symbol."
}
