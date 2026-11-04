package com.julesmeister.deutschcraft.feature.auth.domain

import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {

    val session: StateFlow<AuthSession>

    /*
     * Progress of the Google redirect -> token exchange, so the
     * sign-in screen can show a spinner or an error after the
     * Custom Tab closes.
     */
    val googleSignInState: StateFlow<GoogleSignInState>

    val isConfigured: Boolean

    suspend fun signIn(
        email: String,
        password: String
    ): AuthResult<Unit>

    suspend fun signUp(
        email: String,
        password: String,
        name: String?
    ): AuthResult<SignUpOutcome>

    /*
     * Confirms the email code. If the password from the
     * preceding sign-up / sign-in attempt is still in memory,
     * the user is signed in straight away and `true` is returned.
     */
    suspend fun confirmSignUp(
        email: String,
        code: String
    ): AuthResult<Boolean>

    suspend fun resendConfirmationCode(
        email: String
    ): AuthResult<Unit>

    suspend fun requestPasswordReset(
        email: String
    ): AuthResult<String?>

    suspend fun confirmPasswordReset(
        email: String,
        code: String,
        newPassword: String
    ): AuthResult<Unit>

    /*
     * Prepares PKCE + state and returns the Cognito authorize
     * URL that jumps straight to Google's account picker.
     */
    suspend fun createGoogleSignInUri(): AuthResult<Uri>

    /*
     * Runs in the repository's own scope so the token exchange
     * survives the activity being recreated.
     */
    fun handleOAuthRedirect(
        uri: Uri
    )

    fun clearGoogleSignInError()

    /*
     * Returns a non-expired access token, refreshing it first
     * when needed. Null for guests.
     */
    suspend fun validAccessToken(): String?

    /*
     * Forces a refresh unless another caller already replaced
     * `failedAccessToken`. Returns the current access token or
     * null if the session could not be refreshed.
     */
    suspend fun refreshAccessToken(
        failedAccessToken: String?
    ): String?

    suspend fun signOut()
}

sealed interface GoogleSignInState {

    data object Idle : GoogleSignInState

    data object Exchanging : GoogleSignInState

    data class Failed(
        val error: AuthError
    ) : GoogleSignInState
}
