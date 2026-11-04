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
        val failedToken =
            response
                .request
                .header(AuthorizationHeader)
                ?.removePrefix(BearerPrefix)
                ?: return null

        if (response.priorResponse != null) {
            return null
        }

        val refreshedToken =
            runBlocking {
                authRepository.refreshAccessToken(
                    failedAccessToken = failedToken
                )
            }
                ?: return null

        return response
            .request
            .newBuilder()
            .header(AuthorizationHeader, "$BearerPrefix$refreshedToken")
            .build()
    }
}
