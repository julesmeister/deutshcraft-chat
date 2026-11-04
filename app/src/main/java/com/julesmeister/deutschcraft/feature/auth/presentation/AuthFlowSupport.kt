package com.julesmeister.deutschcraft.feature.auth.presentation

import android.content.Context
import android.net.Uri
import com.julesmeister.deutschcraft.feature.auth.data.GoogleSignInLauncher
import com.julesmeister.deutschcraft.feature.auth.domain.AuthRepository
import com.julesmeister.deutschcraft.feature.auth.domain.AuthResult
import com.julesmeister.deutschcraft.feature.auth.domain.AuthSession
import com.julesmeister.deutschcraft.feature.auth.domain.GoogleSignInState
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

/*
 * Shared by the sign-in and sign-up screens: both offer
 * "Continue with Google" and both close once a session exists.
 */
class AuthFlowSupport
@Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignInLauncher: GoogleSignInLauncher
) {

    val googleSignInState: StateFlow<GoogleSignInState> =
        authRepository.googleSignInState

    val isConfigured: Boolean
        get() = authRepository.isConfigured

    suspend fun prepareGoogleSignIn(): AuthResult<Uri> =
        authRepository.createGoogleSignInUri()

    fun launchGoogleSignIn(
        context: Context,
        uri: Uri,
        toolbarColor: Int
    ) {
        googleSignInLauncher.launch(
            context = context,
            uri = uri,
            toolbarColor = toolbarColor
        )
    }

    fun clearGoogleError() {
        authRepository.clearGoogleSignInError()
    }

    suspend fun awaitSignedIn() {
        authRepository
            .session
            .first { session ->
                session is AuthSession.SignedIn
            }
    }
}

sealed interface AuthUiEvent {

    data object Authenticated : AuthUiEvent

    data class LaunchGoogle(
        val uri: Uri
    ) : AuthUiEvent

    data class OpenVerifyEmail(
        val email: String
    ) : AuthUiEvent

    data class ReturnToSignIn(
        val email: String,
        val notice: String
    ) : AuthUiEvent
}

/*
 * SavedStateHandle keys a later auth screen writes into the
 * sign-in back-stack entry before popping back to it.
 */
object AuthResultKeys {

    const val Notice = "auth_notice"
    const val Email = "auth_email"
}
