package com.julesmeister.deutschcraft.feature.auth.data.remote

import com.julesmeister.deutschcraft.feature.auth.domain.AuthError
import java.io.IOException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/*
 * Turns Cognito / OAuth failures into domain errors.
 *
 * Cognito returns `__type` like
 * "com.amazonaws...#NotAuthorizedException" or just
 * "NotAuthorizedException".
 */
internal object CognitoErrorMapper {

    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    fun map(
        throwable: Throwable
    ): AuthError =
        when (throwable) {
            is HttpException ->
                mapHttp(throwable)

            is IOException ->
                AuthError.Network

            else ->
                AuthError.Unknown(throwable.message)
        }

    fun mapCognitoType(
        type: String?,
        message: String?
    ): AuthError {
        val name =
            type
                ?.substringAfterLast('#')
                ?.substringAfterLast(':')
                ?.trim()
                .orEmpty()

        return when (name) {
            "NotAuthorizedException" ->
                if (message?.contains("Refresh Token", ignoreCase = true) == true) {
                    AuthError.SessionExpired
                } else {
                    AuthError.InvalidCredentials
                }

            "UserNotConfirmedException" ->
                AuthError.UserNotConfirmed

            "UsernameExistsException",
            "AliasExistsException" ->
                AuthError.UserAlreadyExists

            "UserNotFoundException" ->
                AuthError.UserNotFound

            "InvalidPasswordException" ->
                AuthError.InvalidPassword(message)

            "CodeMismatchException" ->
                AuthError.InvalidCode

            "ExpiredCodeException" ->
                AuthError.ExpiredCode

            "LimitExceededException",
            "TooManyRequestsException",
            "TooManyFailedAttemptsException",
            "CodeDeliveryFailureException" ->
                AuthError.TooManyAttempts

            "PasswordResetRequiredException" ->
                AuthError.Unknown("Please reset your password to continue.")

            "InvalidParameterException" ->
                if (message?.contains("password", ignoreCase = true) == true) {
                    AuthError.InvalidPassword(message)
                } else {
                    AuthError.Unknown(message)
                }

            "ResourceNotFoundException" ->
                AuthError.NotConfigured

            else ->
                AuthError.Unknown(message)
        }
    }

    private fun mapHttp(
        exception: HttpException
    ): AuthError {
        val body =
            runCatching {
                exception
                    .response()
                    ?.errorBody()
                    ?.string()
            }.getOrNull()

        if (body.isNullOrBlank()) {
            return if (exception.code() >= 500) {
                AuthError.Network
            } else {
                AuthError.Unknown("HTTP ${exception.code()}")
            }
        }

        val cognitoError =
            runCatching {
                json.decodeFromString(CognitoErrorBody.serializer(), body)
            }.getOrNull()

        if (cognitoError?.type != null) {
            return mapCognitoType(
                type = cognitoError.type,
                message = cognitoError.message ?: cognitoError.messageUpper
            )
        }

        val oauthError =
            runCatching {
                json.decodeFromString(OAuthErrorBody.serializer(), body)
            }.getOrNull()

        return when (oauthError?.error) {
            "invalid_grant" ->
                AuthError.SessionExpired

            "invalid_client",
            "unauthorized_client" ->
                AuthError.NotConfigured

            null ->
                AuthError.Unknown("HTTP ${exception.code()}")

            else ->
                AuthError.Unknown(oauthError.errorDescription ?: oauthError.error)
        }
    }
}
