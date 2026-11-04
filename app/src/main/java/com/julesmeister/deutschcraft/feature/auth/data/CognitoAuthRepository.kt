package com.julesmeister.deutschcraft.feature.auth.data

import android.net.Uri
import com.julesmeister.deutschcraft.feature.auth.data.crypto.JwtDecoder
import com.julesmeister.deutschcraft.feature.auth.data.crypto.Pkce
import com.julesmeister.deutschcraft.feature.auth.data.local.PendingOAuth
import com.julesmeister.deutschcraft.feature.auth.data.local.StoredTokens
import com.julesmeister.deutschcraft.feature.auth.data.local.TokenStore
import com.julesmeister.deutschcraft.feature.auth.data.remote.CognitoAttribute
import com.julesmeister.deutschcraft.feature.auth.data.remote.CognitoAuthApi
import com.julesmeister.deutschcraft.feature.auth.data.remote.CognitoErrorMapper
import com.julesmeister.deutschcraft.feature.auth.data.remote.CognitoOAuthApi
import com.julesmeister.deutschcraft.feature.auth.data.remote.ConfirmForgotPasswordRequest
import com.julesmeister.deutschcraft.feature.auth.data.remote.ConfirmSignUpRequest
import com.julesmeister.deutschcraft.feature.auth.data.remote.ForgotPasswordRequest
import com.julesmeister.deutschcraft.feature.auth.data.remote.InitiateAuthRequest
import com.julesmeister.deutschcraft.feature.auth.data.remote.ResendConfirmationCodeRequest
import com.julesmeister.deutschcraft.feature.auth.data.remote.RevokeTokenRequest
import com.julesmeister.deutschcraft.feature.auth.data.remote.SignUpRequest
import com.julesmeister.deutschcraft.feature.auth.domain.AuthError
import com.julesmeister.deutschcraft.feature.auth.domain.AuthProvider
import com.julesmeister.deutschcraft.feature.auth.domain.AuthRepository
import com.julesmeister.deutschcraft.feature.auth.domain.AuthResult
import com.julesmeister.deutschcraft.feature.auth.domain.AuthSession
import com.julesmeister.deutschcraft.feature.auth.domain.GoogleSignInState
import com.julesmeister.deutschcraft.feature.auth.domain.SignUpOutcome
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class CognitoAuthRepository
@Inject constructor(
    private val config: CognitoConfig,
    private val authApi: CognitoAuthApi,
    private val oauthApi: CognitoOAuthApi,
    private val tokenStore: TokenStore,
    private val googleSignInLauncher: GoogleSignInLauncher
) : AuthRepository {

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val mutableSession =
        MutableStateFlow<AuthSession>(AuthSession.Loading)

    override val session: StateFlow<AuthSession> =
        mutableSession.asStateFlow()

    private val mutableGoogleSignInState =
        MutableStateFlow<GoogleSignInState>(GoogleSignInState.Idle)

    override val googleSignInState: StateFlow<GoogleSignInState> =
        mutableGoogleSignInState.asStateFlow()

    override val isConfigured: Boolean
        get() = config.isPasswordAuthConfigured

    private val refreshMutex =
        Mutex()

    @Volatile
    private var cachedTokens: StoredTokens? =
        null

    /*
     * Kept in memory only, so that verifying the email code can
     * sign the user in without asking for the password again.
     */
    @Volatile
    private var pendingCredentials: PendingCredentials? =
        null

    private val initialLoad =
        scope.async {
            val tokens =
                tokenStore.readTokens()

            cachedTokens = tokens

            mutableSession.value =
                tokens?.toSession() ?: AuthSession.Guest
        }

    /*
     * =======================================================
     * Email + password
     * =======================================================
     */

    override suspend fun signIn(
        email: String,
        password: String
    ): AuthResult<Unit> {
        if (!config.isPasswordAuthConfigured) {
            return AuthResult.Failure(AuthError.NotConfigured)
        }

        val username =
            email.normalizedEmail()

        pendingCredentials =
            PendingCredentials(username, password)

        val result =
            runAuth {
                val response =
                    authApi.initiateAuth(
                        InitiateAuthRequest(
                            authFlow = CognitoAuthApi.AuthFlowUserPassword,
                            clientId = config.clientId,
                            authParameters = mapOf(
                                "USERNAME" to username,
                                "PASSWORD" to password
                            )
                        )
                    )

                val authentication =
                    response.authenticationResult
                        ?: throw AuthFailure(challengeError(response.challengeName))

                val refreshToken =
                    authentication.refreshToken
                        ?: throw AuthFailure(AuthError.Unknown("Cognito did not return a refresh token."))

                saveTokens(
                    accessToken = authentication.accessToken,
                    idToken = authentication.idToken,
                    refreshToken = refreshToken,
                    expiresInSeconds = authentication.expiresIn,
                    provider = AuthProvider.EMAIL
                )
            }

        if (result is AuthResult.Success) {
            pendingCredentials = null
        }

        return result
    }

    override suspend fun signUp(
        email: String,
        password: String,
        name: String?
    ): AuthResult<SignUpOutcome> {
        if (!config.isPasswordAuthConfigured) {
            return AuthResult.Failure(AuthError.NotConfigured)
        }

        val username =
            email.normalizedEmail()

        val attributes =
            buildList {
                add(CognitoAttribute("email", username))

                name
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { add(CognitoAttribute("name", it)) }
            }

        val signUpResult =
            runAuth {
                authApi.signUp(
                    SignUpRequest(
                        clientId = config.clientId,
                        username = username,
                        password = password,
                        userAttributes = attributes
                    )
                )
            }

        return when (signUpResult) {
            is AuthResult.Failure ->
                signUpResult

            is AuthResult.Success -> {
                pendingCredentials =
                    PendingCredentials(username, password)

                val response =
                    signUpResult.value

                if (response.userConfirmed) {
                    signIn(username, password)
                }

                AuthResult.Success(
                    SignUpOutcome(
                        needsConfirmation = !response.userConfirmed,
                        codeDestination = response.codeDeliveryDetails?.destination
                    )
                )
            }
        }
    }

    override suspend fun confirmSignUp(
        email: String,
        code: String
    ): AuthResult<Boolean> {
        if (!config.isPasswordAuthConfigured) {
            return AuthResult.Failure(AuthError.NotConfigured)
        }

        val username =
            email.normalizedEmail()

        val confirmResult =
            runAuth {
                authApi.confirmSignUp(
                    ConfirmSignUpRequest(
                        clientId = config.clientId,
                        username = username,
                        confirmationCode = code.trim()
                    )
                )
            }

        if (confirmResult is AuthResult.Failure) {
            return confirmResult
        }

        val credentials =
            pendingCredentials
                ?.takeIf { it.email == username }
                ?: return AuthResult.Success(false)

        val signedIn =
            signIn(credentials.email, credentials.password) is AuthResult.Success

        return AuthResult.Success(signedIn)
    }

    override suspend fun resendConfirmationCode(
        email: String
    ): AuthResult<Unit> {
        if (!config.isPasswordAuthConfigured) {
            return AuthResult.Failure(AuthError.NotConfigured)
        }

        return runAuth {
            authApi.resendConfirmationCode(
                ResendConfirmationCodeRequest(
                    clientId = config.clientId,
                    username = email.normalizedEmail()
                )
            )

            Unit
        }
    }

    override suspend fun requestPasswordReset(
        email: String
    ): AuthResult<String?> {
        if (!config.isPasswordAuthConfigured) {
            return AuthResult.Failure(AuthError.NotConfigured)
        }

        return runAuth {
            authApi
                .forgotPassword(
                    ForgotPasswordRequest(
                        clientId = config.clientId,
                        username = email.normalizedEmail()
                    )
                )
                .codeDeliveryDetails
                ?.destination
        }
    }

    override suspend fun confirmPasswordReset(
        email: String,
        code: String,
        newPassword: String
    ): AuthResult<Unit> {
        if (!config.isPasswordAuthConfigured) {
            return AuthResult.Failure(AuthError.NotConfigured)
        }

        return runAuth {
            authApi.confirmForgotPassword(
                ConfirmForgotPasswordRequest(
                    clientId = config.clientId,
                    username = email.normalizedEmail(),
                    confirmationCode = code.trim(),
                    password = newPassword
                )
            )

            Unit
        }
    }

    /*
     * =======================================================
     * Google (Cognito OAuth + PKCE)
     * =======================================================
     */

    override suspend fun createGoogleSignInUri(): AuthResult<Uri> {
        if (!config.isHostedUiConfigured) {
            return AuthResult.Failure(AuthError.NotConfigured)
        }

        val pkce =
            Pkce.generate()

        val state =
            Pkce.generateState()

        return runAuth {
            tokenStore.writePendingOAuth(
                PendingOAuth(
                    codeVerifier = pkce.verifier,
                    state = state,
                    createdAtEpochMillis = System.currentTimeMillis()
                )
            )

            mutableGoogleSignInState.value =
                GoogleSignInState.Idle

            googleSignInLauncher.buildAuthorizeUri(
                pkce = pkce,
                state = state
            )
        }
    }

    override fun handleOAuthRedirect(
        uri: Uri
    ) {
        scope.launch {
            completeOAuthRedirect(uri)
        }
    }

    private suspend fun completeOAuthRedirect(
        uri: Uri
    ) {
        if (uri.path != CognitoConfig.RedirectCallbackPath) {
            return
        }

        val error =
            uri.getQueryParameter("error")

        if (error != null) {
            tokenStore.clearPendingOAuth()

            mutableGoogleSignInState.value =
                if (error == "access_denied") {
                    GoogleSignInState.Idle
                } else {
                    GoogleSignInState.Failed(
                        AuthError.Unknown(
                            uri.getQueryParameter("error_description") ?: error
                        )
                    )
                }

            return
        }

        val code =
            uri.getQueryParameter("code")
                ?: return

        val pending =
            tokenStore.readPendingOAuth()

        if (pending == null) {
            // Already handled (e.g. the activity was recreated).
            return
        }

        tokenStore.clearPendingOAuth()

        val isExpired =
            System.currentTimeMillis() - pending.createdAtEpochMillis > PendingOAuthMaxAgeMillis

        if (pending.state != uri.getQueryParameter("state") || isExpired) {
            mutableGoogleSignInState.value =
                GoogleSignInState.Failed(
                    AuthError.Unknown("That sign-in link has expired. Please try again.")
                )

            return
        }

        mutableGoogleSignInState.value =
            GoogleSignInState.Exchanging

        val result =
            runAuth {
                val response =
                    oauthApi.exchangeAuthorizationCode(
                        clientId = config.clientId,
                        code = code,
                        redirectUri = CognitoConfig.RedirectUri,
                        codeVerifier = pending.codeVerifier
                    )

                val refreshToken =
                    response.refreshToken
                        ?: throw AuthFailure(AuthError.Unknown("Cognito did not return a refresh token."))

                saveTokens(
                    accessToken = response.accessToken,
                    idToken = response.idToken,
                    refreshToken = refreshToken,
                    expiresInSeconds = response.expiresIn,
                    provider = AuthProvider.GOOGLE
                )
            }

        mutableGoogleSignInState.value =
            when (result) {
                is AuthResult.Success -> GoogleSignInState.Idle
                is AuthResult.Failure -> GoogleSignInState.Failed(result.error)
            }
    }

    override fun clearGoogleSignInError() {
        if (mutableGoogleSignInState.value is GoogleSignInState.Failed) {
            mutableGoogleSignInState.value =
                GoogleSignInState.Idle
        }
    }

    /*
     * =======================================================
     * Tokens
     * =======================================================
     */

    override suspend fun validAccessToken(): String? {
        val tokens =
            currentTokens()
                ?: return null

        if (!tokens.isNearExpiry()) {
            return tokens.accessToken
        }

        return refreshAccessToken(
            failedAccessToken = tokens.accessToken
        )
    }

    override suspend fun refreshAccessToken(
        failedAccessToken: String?
    ): String? =
        refreshMutex.withLock {
            val tokens =
                currentTokens()
                    ?: return@withLock null

            /*
             * Another request refreshed while this one waited
             * for the lock.
             */
            if (tokens.accessToken != failedAccessToken && !tokens.isNearExpiry()) {
                return@withLock tokens.accessToken
            }

            val result =
                runAuth {
                    val response =
                        authApi.initiateAuth(
                            InitiateAuthRequest(
                                authFlow = CognitoAuthApi.AuthFlowRefreshToken,
                                clientId = config.clientId,
                                authParameters = mapOf(
                                    "REFRESH_TOKEN" to tokens.refreshToken
                                )
                            )
                        )

                    val authentication =
                        response.authenticationResult
                            ?: throw AuthFailure(AuthError.SessionExpired)

                    saveTokens(
                        accessToken = authentication.accessToken,
                        idToken = authentication.idToken,
                        // Cognito does not rotate refresh tokens by default.
                        refreshToken = authentication.refreshToken ?: tokens.refreshToken,
                        expiresInSeconds = authentication.expiresIn,
                        provider = AuthProvider.valueOf(tokens.provider)
                    )

                    authentication.accessToken
                }

            when (result) {
                is AuthResult.Success ->
                    result.value

                is AuthResult.Failure -> {
                    if (
                        result.error == AuthError.SessionExpired ||
                        result.error == AuthError.InvalidCredentials
                    ) {
                        clearLocalSession()
                    }

                    null
                }
            }
        }

    override suspend fun signOut() {
        val tokens =
            currentTokens()

        clearLocalSession()

        if (tokens != null && config.isPasswordAuthConfigured) {
            scope.launch {
                // Best effort: invalidates the refresh token server-side.
                runCatching {
                    authApi.revokeToken(
                        RevokeTokenRequest(
                            clientId = config.clientId,
                            token = tokens.refreshToken
                        )
                    )
                }
            }
        }
    }

    /*
     * =======================================================
     * Helpers
     * =======================================================
     */

    private suspend fun currentTokens(): StoredTokens? {
        initialLoad.await()

        return cachedTokens
    }

    private suspend fun saveTokens(
        accessToken: String,
        idToken: String,
        refreshToken: String,
        expiresInSeconds: Long,
        provider: AuthProvider
    ) {
        val tokens =
            StoredTokens(
                accessToken = accessToken,
                idToken = idToken,
                refreshToken = refreshToken,
                expiresAtEpochMillis =
                    System.currentTimeMillis() + expiresInSeconds * 1_000L,
                provider = provider.name
            )

        tokenStore.writeTokens(tokens)

        cachedTokens = tokens

        mutableSession.value =
            tokens.toSession()
    }

    private suspend fun clearLocalSession() {
        tokenStore.clearTokens()

        cachedTokens = null
        pendingCredentials = null

        mutableSession.value =
            AuthSession.Guest
    }

    private fun StoredTokens.toSession(): AuthSession {
        val claims =
            JwtDecoder.decode(idToken)

        val provider =
            runCatching { AuthProvider.valueOf(provider) }
                .getOrDefault(AuthProvider.EMAIL)

        return AuthSession.SignedIn(
            email = claims?.email.orEmpty(),
            displayName = claims?.name,
            provider = provider
        )
    }

    private fun StoredTokens.isNearExpiry(): Boolean =
        expiresAtEpochMillis - System.currentTimeMillis() <= RefreshLeewayMillis

    private fun challengeError(
        challengeName: String?
    ): AuthError =
        when (challengeName) {
            "NEW_PASSWORD_REQUIRED" ->
                AuthError.Unknown("This account needs a new password. Use “Forgot password?” to set one.")

            else ->
                AuthError.Unknown("Additional verification ($challengeName) is not supported yet.")
        }

    private suspend fun <T> runAuth(
        block: suspend () -> T
    ): AuthResult<T> =
        try {
            AuthResult.Success(block())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: AuthFailure) {
            AuthResult.Failure(failure.error)
        } catch (throwable: Throwable) {
            AuthResult.Failure(CognitoErrorMapper.map(throwable))
        }

    private fun String.normalizedEmail(): String =
        trim().lowercase()

    private class AuthFailure(
        val error: AuthError
    ) : Exception()

    private data class PendingCredentials(
        val email: String,
        val password: String
    ) {
        override fun toString(): String =
            "PendingCredentials(email=$email)"
    }

    private companion object {

        const val RefreshLeewayMillis =
            60_000L

        const val PendingOAuthMaxAgeMillis =
            15 * 60_000L
    }
}
