// app/src/main/java/com/julesmeister/deutschcraft/feature/auth/data/CognitoAuthRepository.kt

package com.julesmeister.deutschcraft.feature.auth.data

import android.net.Uri
import com.julesmeister.deutschcraft.feature.auth.data.crypto.JwtDecoder
import com.julesmeister.deutschcraft.feature.auth.data.crypto.Pkce
import com.julesmeister.deutschcraft.feature.auth.data.local.PendingOAuth
import com.julesmeister.deutschcraft.feature.auth.data.local.StoredTokens
import com.julesmeister.deutschcraft.feature.auth.data.local.TokenStore
import com.julesmeister.deutschcraft.feature.auth.data.network.AuthDiagnostics
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
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@Singleton
class CognitoAuthRepository
@Inject constructor(
    private val config: CognitoConfig,
    private val authApi: CognitoAuthApi,
    private val oauthApi: CognitoOAuthApi,
    private val tokenStore: TokenStore,
    private val googleSignInLauncher:
    GoogleSignInLauncher,
    private val logoutLauncher:
    CognitoLogoutLauncher
) : AuthRepository {

    private val scope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO
        )

    private val mutableSession =
        MutableStateFlow<AuthSession>(
            AuthSession.Loading
        )

    override val session:
            StateFlow<AuthSession> =
        mutableSession.asStateFlow()

    private val mutableGoogleSignInState =
        MutableStateFlow<GoogleSignInState>(
            GoogleSignInState.Idle
        )

    override val googleSignInState:
            StateFlow<GoogleSignInState> =
        mutableGoogleSignInState.asStateFlow()

    override val isConfigured: Boolean
        get() =
            config.isPasswordAuthConfigured

    private val refreshMutex =
        Mutex()

    @Volatile
    private var cachedTokens:
            StoredTokens? = null

    @Volatile
    private var pendingCredentials:
            PendingCredentials? = null

    private val initialLoad =
        scope.async {
            val tokens =
                tokenStore.readTokens()

            cachedTokens =
                tokens

            mutableSession.value =
                tokens?.toSession()
                    ?: AuthSession.Guest
        }

    override suspend fun signIn(
        email: String,
        password: String
    ): AuthResult<Unit> {
        if (!config.isPasswordAuthConfigured) {
            return AuthResult.Failure(
                AuthError.NotConfigured
            )
        }

        val username =
            email.normalizedEmail()

        pendingCredentials =
            PendingCredentials(
                email = username,
                password = password
            )

        val result =
            runAuth {
                val response =
                    authApi.initiateAuth(
                        InitiateAuthRequest(
                            authFlow =
                                CognitoAuthApi
                                    .AuthFlowUserPassword,
                            clientId =
                                config.clientId,
                            authParameters =
                                mapOf(
                                    "USERNAME" to username,
                                    "PASSWORD" to password
                                )
                        )
                    )

                val authentication =
                    response.authenticationResult
                        ?: throw AuthFailure(
                            challengeError(
                                response.challengeName
                            )
                        )

                val refreshToken =
                    authentication.refreshToken
                        ?: throw AuthFailure(
                            AuthError.Unknown(
                                "Cognito did not return a refresh token."
                            )
                        )

                saveTokens(
                    accessToken =
                        authentication.accessToken,
                    idToken =
                        authentication.idToken,
                    refreshToken =
                        refreshToken,
                    expiresInSeconds =
                        authentication.expiresIn,
                    provider =
                        AuthProvider.EMAIL
                )
            }

        if (result is AuthResult.Success) {
            pendingCredentials =
                null
        }

        return result
    }

    override suspend fun signUp(
        email: String,
        password: String,
        name: String?
    ): AuthResult<SignUpOutcome> {
        if (!config.isPasswordAuthConfigured) {
            return AuthResult.Failure(
                AuthError.NotConfigured
            )
        }

        val username =
            email.normalizedEmail()

        val attributes =
            buildList {
                add(
                    CognitoAttribute(
                        "email",
                        username
                    )
                )

                name
                    ?.trim()
                    ?.takeIf(String::isNotBlank)
                    ?.let { cleanName ->
                        add(
                            CognitoAttribute(
                                "name",
                                cleanName
                            )
                        )
                    }
            }

        val signUpResult =
            runAuth {
                authApi.signUp(
                    SignUpRequest(
                        clientId =
                            config.clientId,
                        username =
                            username,
                        password =
                            password,
                        userAttributes =
                            attributes
                    )
                )
            }

        return when (signUpResult) {
            is AuthResult.Failure ->
                signUpResult

            is AuthResult.Success -> {
                pendingCredentials =
                    PendingCredentials(
                        email = username,
                        password = password
                    )

                val response =
                    signUpResult.value

                if (response.userConfirmed) {
                    signIn(
                        email = username,
                        password = password
                    )
                }

                AuthResult.Success(
                    SignUpOutcome(
                        needsConfirmation =
                            !response.userConfirmed,
                        codeDestination =
                            response
                                .codeDeliveryDetails
                                ?.destination
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
            return AuthResult.Failure(
                AuthError.NotConfigured
            )
        }

        val username =
            email.normalizedEmail()

        val confirmationResult =
            runAuth {
                authApi.confirmSignUp(
                    ConfirmSignUpRequest(
                        clientId =
                            config.clientId,
                        username =
                            username,
                        confirmationCode =
                            code.trim()
                    )
                )
            }

        if (
            confirmationResult
                    is AuthResult.Failure
        ) {
            return confirmationResult
        }

        val credentials =
            pendingCredentials
                ?.takeIf { pending ->
                    pending.email ==
                            username
                }
                ?: return AuthResult.Success(
                    false
                )

        val signedIn =
            signIn(
                email = credentials.email,
                password =
                    credentials.password
            ) is AuthResult.Success

        return AuthResult.Success(
            signedIn
        )
    }

    override suspend fun resendConfirmationCode(
        email: String
    ): AuthResult<Unit> {
        if (!config.isPasswordAuthConfigured) {
            return AuthResult.Failure(
                AuthError.NotConfigured
            )
        }

        return runAuth {
            authApi.resendConfirmationCode(
                ResendConfirmationCodeRequest(
                    clientId =
                        config.clientId,
                    username =
                        email.normalizedEmail()
                )
            )

            Unit
        }
    }

    override suspend fun requestPasswordReset(
        email: String
    ): AuthResult<String?> {
        if (!config.isPasswordAuthConfigured) {
            return AuthResult.Failure(
                AuthError.NotConfigured
            )
        }

        return runAuth {
            authApi
                .forgotPassword(
                    ForgotPasswordRequest(
                        clientId =
                            config.clientId,
                        username =
                            email.normalizedEmail()
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
            return AuthResult.Failure(
                AuthError.NotConfigured
            )
        }

        return runAuth {
            authApi.confirmForgotPassword(
                ConfirmForgotPasswordRequest(
                    clientId =
                        config.clientId,
                    username =
                        email.normalizedEmail(),
                    confirmationCode =
                        code.trim(),
                    password =
                        newPassword
                )
            )

            Unit
        }
    }

    override suspend fun createGoogleSignInUri():
            AuthResult<Uri> {
        if (!config.isHostedUiConfigured) {
            return AuthResult.Failure(
                AuthError.NotConfigured
            )
        }

        val pkce =
            Pkce.generate()

        val state =
            Pkce.generateState()

        return runAuth {
            tokenStore.writePendingOAuth(
                PendingOAuth(
                    codeVerifier =
                        pkce.verifier,
                    state =
                        state,
                    createdAtEpochMillis =
                        System.currentTimeMillis()
                )
            )

            mutableGoogleSignInState.value =
                GoogleSignInState.Idle

            googleSignInLauncher
                .buildAuthorizeUri(
                    pkce = pkce,
                    state = state
                )
        }
    }

    override fun handleOAuthRedirect(
        uri: Uri
    ) {
        scope.launch {
            completeOAuthRedirect(
                uri = uri
            )
        }
    }

    private suspend fun completeOAuthRedirect(
        uri: Uri
    ) {
        if (
            uri.path !=
            CognitoConfig.RedirectCallbackPath
        ) {
            return
        }

        val error =
            uri.getQueryParameter(
                "error"
            )

        if (error != null) {
            tokenStore.clearPendingOAuth()

            mutableGoogleSignInState.value =
                if (
                    error ==
                    "access_denied"
                ) {
                    GoogleSignInState.Idle
                } else {
                    GoogleSignInState.Failed(
                        AuthError.Unknown(
                            uri.getQueryParameter(
                                "error_description"
                            ) ?: error
                        )
                    )
                }

            return
        }

        val code =
            uri.getQueryParameter(
                "code"
            ) ?: return

        val pending =
            tokenStore.readPendingOAuth()
                ?: return

        tokenStore.clearPendingOAuth()

        val isExpired =
            System.currentTimeMillis() -
                    pending.createdAtEpochMillis >
                    PENDING_OAUTH_MAX_AGE_MILLIS

        if (
            pending.state !=
            uri.getQueryParameter("state") ||
            isExpired
        ) {
            mutableGoogleSignInState.value =
                GoogleSignInState.Failed(
                    AuthError.Unknown(
                        "That sign-in link has expired. Please try again."
                    )
                )

            return
        }

        mutableGoogleSignInState.value =
            GoogleSignInState.Exchanging

        val result =
            runAuth {
                val response =
                    oauthApi.exchangeAuthorizationCode(
                        clientId =
                            config.clientId,
                        code =
                            code,
                        redirectUri =
                            CognitoConfig.RedirectUri,
                        codeVerifier =
                            pending.codeVerifier
                    )

                val refreshToken =
                    response.refreshToken
                        ?: throw AuthFailure(
                            AuthError.Unknown(
                                "Cognito did not return a refresh token."
                            )
                        )

                saveTokens(
                    accessToken =
                        response.accessToken,
                    idToken =
                        response.idToken,
                    refreshToken =
                        refreshToken,
                    expiresInSeconds =
                        response.expiresIn,
                    provider =
                        AuthProvider.GOOGLE
                )
            }

        mutableGoogleSignInState.value =
            when (result) {
                is AuthResult.Success ->
                    GoogleSignInState.Idle

                is AuthResult.Failure ->
                    GoogleSignInState.Failed(
                        result.error
                    )
            }
    }

    override fun clearGoogleSignInError() {
        if (
            mutableGoogleSignInState.value
                    is GoogleSignInState.Failed
        ) {
            mutableGoogleSignInState.value =
                GoogleSignInState.Idle
        }
    }

    override suspend fun validAccessToken():
            String? {
        val tokens =
            currentTokens()
                ?: run {
                    AuthDiagnostics.event(
                        "access-token-unavailable reason=no-stored-session"
                    )

                    return null
                }

        if (!tokens.isNearExpiry()) {
            return tokens.accessToken
        }

        AuthDiagnostics.event(
            "refresh-requested reason=near-local-expiry"
        )

        return refreshAccessToken(
            failedAccessToken =
                tokens.accessToken
        )
    }

    override suspend fun refreshAccessToken(
        failedAccessToken: String?
    ): String? =
        refreshMutex.withLock {
            val tokens =
                currentTokens()
                    ?: run {
                        AuthDiagnostics.event(
                            "refresh-skipped reason=no-stored-session"
                        )

                        return@withLock null
                    }

            if (
                tokens.accessToken !=
                failedAccessToken &&
                !tokens.isNearExpiry()
            ) {
                AuthDiagnostics.event(
                    "refresh-reused reason=another-request-already-refreshed"
                )

                return@withLock tokens.accessToken
            }

            AuthDiagnostics.event(
                "refresh-started flow=REFRESH_TOKEN_AUTH"
            )

            val result =
                runAuth {
                    val response =
                        authApi.initiateAuth(
                            InitiateAuthRequest(
                                authFlow =
                                    CognitoAuthApi
                                        .AuthFlowRefreshToken,
                                clientId =
                                    config.clientId,
                                authParameters =
                                    mapOf(
                                        "REFRESH_TOKEN" to
                                                tokens.refreshToken
                                    )
                            )
                        )

                    val authentication =
                        response.authenticationResult
                            ?: throw AuthFailure(
                                AuthError.SessionExpired
                            )

                    saveTokens(
                        accessToken =
                            authentication.accessToken,
                        idToken =
                            authentication.idToken,
                        refreshToken =
                            authentication.refreshToken
                                ?: tokens.refreshToken,
                        expiresInSeconds =
                            authentication.expiresIn,
                        provider =
                            AuthProvider.valueOf(
                                tokens.provider
                            )
                    )

                    authentication.accessToken
                }

            when (result) {
                is AuthResult.Success -> {
                    AuthDiagnostics.event(
                        "refresh-succeeded"
                    )

                    result.value
                }

                is AuthResult.Failure -> {
                    AuthDiagnostics.event(
                        "refresh-failed category=" +
                                result.error
                                    .javaClass
                                    .simpleName
                    )

                    if (
                        result.error ==
                        AuthError.SessionExpired ||
                        result.error ==
                        AuthError.InvalidCredentials
                    ) {
                        AuthDiagnostics.event(
                            "session-cleared reason=refresh-credentials-rejected"
                        )

                        clearLocalSession()
                    }

                    null
                }
            }
        }

    override suspend fun signOut() {
        val tokens =
            currentTokens()

        val wasGoogleSession =
            tokens?.provider ==
                    AuthProvider.GOOGLE.name

        clearLocalSession()

        if (
            tokens != null &&
            config.isPasswordAuthConfigured
        ) {
            scope.launch {
                runCatching {
                    authApi.revokeToken(
                        RevokeTokenRequest(
                            clientId =
                                config.clientId,
                            token =
                                tokens.refreshToken
                        )
                    )
                }
            }
        }

        if (wasGoogleSession) {
            withContext(
                Dispatchers.Main
            ) {
                logoutLauncher.launch()
            }
        }
    }

    private suspend fun currentTokens():
            StoredTokens? {
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
                    System.currentTimeMillis() +
                            expiresInSeconds *
                            1_000L,
                provider = provider.name
            )

        tokenStore.writeTokens(
            tokens
        )

        cachedTokens =
            tokens

        mutableSession.value =
            tokens.toSession()
    }

    private suspend fun clearLocalSession() {
        tokenStore.clearTokens()
        tokenStore.clearPendingOAuth()

        cachedTokens =
            null

        pendingCredentials =
            null

        mutableGoogleSignInState.value =
            GoogleSignInState.Idle

        mutableSession.value =
            AuthSession.Guest
    }

    private fun StoredTokens.toSession():
            AuthSession {
        val claims =
            JwtDecoder.decode(
                idToken
            )

        val userId =
            claims
                ?.subject
                ?.trim()
                .orEmpty()

        if (userId.isBlank()) {
            return AuthSession.Guest
        }

        val storedProvider =
            runCatching {
                AuthProvider.valueOf(
                    provider
                )
            }.getOrDefault(
                AuthProvider.EMAIL
            )

        return AuthSession.SignedIn(
            userId = userId,
            email =
                claims?.email.orEmpty(),
            displayName =
                claims?.name,
            provider =
                storedProvider
        )
    }

    private fun StoredTokens.isNearExpiry():
            Boolean {
        return expiresAtEpochMillis -
                System.currentTimeMillis() <=
                REFRESH_LEEWAY_MILLIS
    }

    private fun challengeError(
        challengeName: String?
    ): AuthError {
        return when (challengeName) {
            "NEW_PASSWORD_REQUIRED" ->
                AuthError.Unknown(
                    "This account needs a new password. " +
                            "Use “Forgot password?” to set one."
                )

            else ->
                AuthError.Unknown(
                    "Additional verification " +
                            "($challengeName) is not supported yet."
                )
        }
    }

    private suspend fun <T> runAuth(
        block: suspend () -> T
    ): AuthResult<T> {
        return try {
            AuthResult.Success(
                block()
            )
        } catch (
            cancellation:
            CancellationException
        ) {
            throw cancellation
        } catch (
            failure:
            AuthFailure
        ) {
            AuthResult.Failure(
                failure.error
            )
        } catch (throwable: Throwable) {
            AuthDiagnostics.event(
                "cognito-call-failed exceptionType=" +
                        throwable.javaClass.simpleName +
                        " httpStatus=" +
                        (throwable as? HttpException)
                            ?.code()
            )

            AuthResult.Failure(
                CognitoErrorMapper.map(
                    throwable
                )
            )
        }
    }

    private fun String.normalizedEmail():
            String {
        return trim().lowercase()
    }

    private class AuthFailure(
        val error: AuthError
    ) : Exception()

    private data class PendingCredentials(
        val email: String,
        val password: String
    ) {
        override fun toString():
                String {
            return "PendingCredentials(email=$email)"
        }
    }

    private companion object {
        const val REFRESH_LEEWAY_MILLIS =
            60_000L

        const val PENDING_OAUTH_MAX_AGE_MILLIS =
            15 * 60_000L
    }
}