package com.julesmeister.deutschcraft.feature.auth.data.network

import com.julesmeister.deutschcraft.feature.auth.data.network.AuthInterceptor.Companion.AuthorizationHeader
import com.julesmeister.deutschcraft.feature.auth.data.network.AuthInterceptor.Companion.BearerPrefix
import com.julesmeister.deutschcraft.feature.auth.domain.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/*
 * Called by OkHttp on a 401. Refreshes the Cognito tokens once
 * and retries; a second 401 is returned to the caller.
 */
@Singleton
class TokenAuthenticator
@Inject constructor(
    private val authRepository: AuthRepository
) : Authenticator {

    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {
        AuthDiagnostics.response("unauthorized-response", response)
        val failedToken =
            response
                .request
                .header(AuthorizationHeader)
                ?.removePrefix(BearerPrefix)
                ?: run {
                    AuthDiagnostics.event("retry-skipped reason=no-authorization-header")
                    return null
                }

        if (response.priorResponse != null) {
            AuthDiagnostics.event("retry-stopped reason=prior-response-present")
            return null
        }

        AuthDiagnostics.event("refresh-requested reason=http-401")
        val refreshedToken =
            runBlocking {
                authRepository.refreshAccessToken(
                    failedAccessToken = failedToken
                )
            }
                ?: run {
                    AuthDiagnostics.event("retry-skipped reason=no-refreshed-access-token")
                    return null
                }

        AuthDiagnostics.event("retry-scheduled tokenChanged=${refreshedToken != failedToken}")
        return response
            .request
            .newBuilder()
            .header(AuthorizationHeader, "$BearerPrefix$refreshedToken")
            .build()
    }
}
