package com.julesmeister.deutschcraft.feature.auth.data.network

import com.julesmeister.deutschcraft.feature.auth.domain.AuthRepository
import com.julesmeister.deutschcraft.feature.auth.domain.AuthSession
import com.julesmeister.deutschcraft.BuildConfig
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/*
 * Adds "Authorization: Bearer <access token>" to AI proxy calls
 * when the user is signed in. Guests' requests go out without
 * the header.
 */
@Singleton
class AuthInterceptor
@Inject constructor(
    private val authRepository: AuthRepository
) : Interceptor {

    override fun intercept(
        chain: Interceptor.Chain
    ): Response {
        val request =
            chain.request()

        val authenticatedRequest =
            if (request.header(AuthorizationHeader) != null) {
                request
            } else {
                // OkHttp runs interceptors on its own background threads.
                val accessToken = runBlocking {
                    authRepository.validAccessToken()
                }
                if (accessToken == null) {
                    request
                } else {
                    request.newBuilder()
                        .header(AuthorizationHeader, "$BearerPrefix$accessToken")
                        .build()
                }
            }

        if (BuildConfig.DEBUG) {
            val token = authenticatedRequest.header(AuthorizationHeader)
                ?.takeIf { it.startsWith(BearerPrefix) }
                ?.removePrefix(BearerPrefix)
            AuthDiagnostics.event(
                "request method=${request.method} host=${request.url.host} " +
                    "signedIn=${authRepository.session.value is AuthSession.SignedIn} " +
                    "authorizationPresent=${authenticatedRequest.header(AuthorizationHeader) != null} " +
                    AuthDiagnostics.tokenSummary(token)
            )
        }
        return try {
            chain.proceed(authenticatedRequest).also {
                AuthDiagnostics.response("final-response", it)
            }
        } catch (exception: IOException) {
            AuthDiagnostics.event("request-failed exceptionType=${exception.javaClass.simpleName}")
            throw exception
        }
    }

    companion object {

        const val AuthorizationHeader =
            "Authorization"

        const val BearerPrefix =
            "Bearer "
    }
}
