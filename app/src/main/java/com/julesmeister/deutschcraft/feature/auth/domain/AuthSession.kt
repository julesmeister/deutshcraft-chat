// app/src/main/java/com/julesmeister/deutschcraft/feature/auth/domain/AuthSession.kt

package com.julesmeister.deutschcraft.feature.auth.domain

sealed interface AuthSession {

    /*
     * Stored tokens have not been read yet.
     */
    data object Loading :
        AuthSession

    data object Guest :
        AuthSession

    data class SignedIn(
        /*
         * Cognito JWT "sub".
         *
         * This is the stable account identifier used for
         * account-scoped synchronization state.
         */
        val userId: String,

        val email: String,

        val displayName: String?,

        val provider:
        AuthProvider
    ) : AuthSession
}

enum class AuthProvider {
    EMAIL,
    GOOGLE
}

val AuthSession.isSignedIn:
        Boolean
    get() =
        this is AuthSession.SignedIn