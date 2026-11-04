package com.julesmeister.deutschcraft.feature.auth.data.network

import com.julesmeister.deutschcraft.feature.auth.domain.AuthRepository
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

        if (request.header(AuthorizationHeader) != null) {
            return chain.proceed(request)
        }

        // OkHttp runs interceptors on its own background threads.
        val accessToken =
            runBlocking {
                authRepository.validAccessToken()
            }
                ?: return chain.proceed(request)

        return chain.proceed(
            request
                .newBuilder()
                .header(AuthorizationHeader, "$BearerPrefix$accessToken")
                .build()
        )
    }

    companion object {

        const val AuthorizationHeader =
            "Authorization"

        const val BearerPrefix =
            "Bearer "
    }
}
