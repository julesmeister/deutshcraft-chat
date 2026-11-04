package com.julesmeister.deutschcraft.feature.auth.domain

sealed interface AuthSession {

    /*
     * Stored tokens have not been read yet.
     */
    data object Loading : AuthSession

    data object Guest : AuthSession

    data class SignedIn(
        val email: String,
        val displayName: String?,
        val provider: AuthProvider
    ) : AuthSession
}

enum class AuthProvider {
    EMAIL,
    GOOGLE
}

val AuthSession.isSignedIn: Boolean
    get() =
        this is AuthSession.SignedIn
