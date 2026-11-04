package com.julesmeister.deutschcraft.feature.auth.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Cognito Identity Provider JSON 1.1 bodies. Field names are
 * PascalCase on the wire.
 */

@Serializable
data class CognitoAttribute(
    @SerialName("Name") val name: String,
    @SerialName("Value") val value: String
)

@Serializable
data class SignUpRequest(
    @SerialName("ClientId") val clientId: String,
    @SerialName("Username") val username: String,
    @SerialName("Password") val password: String,
    @SerialName("UserAttributes") val userAttributes: List<CognitoAttribute>
)

@Serializable
data class SignUpResponse(
    @SerialName("UserConfirmed") val userConfirmed: Boolean = false,
    @SerialName("UserSub") val userSub: String? = null,
    @SerialName("CodeDeliveryDetails") val codeDeliveryDetails: CodeDeliveryDetails? = null
)

@Serializable
data class CodeDeliveryDetails(
    @SerialName("Destination") val destination: String? = null,
    @SerialName("DeliveryMedium") val deliveryMedium: String? = null
)

@Serializable
data class ConfirmSignUpRequest(
    @SerialName("ClientId") val clientId: String,
    @SerialName("Username") val username: String,
    @SerialName("ConfirmationCode") val confirmationCode: String
)

@Serializable
data class ResendConfirmationCodeRequest(
    @SerialName("ClientId") val clientId: String,
    @SerialName("Username") val username: String
)

@Serializable
data class CodeDeliveryResponse(
    @SerialName("CodeDeliveryDetails") val codeDeliveryDetails: CodeDeliveryDetails? = null
)

@Serializable
data class InitiateAuthRequest(
    @SerialName("AuthFlow") val authFlow: String,
    @SerialName("ClientId") val clientId: String,
    @SerialName("AuthParameters") val authParameters: Map<String, String>
)

@Serializable
data class InitiateAuthResponse(
    @SerialName("AuthenticationResult") val authenticationResult: AuthenticationResult? = null,
    @SerialName("ChallengeName") val challengeName: String? = null,
    @SerialName("Session") val session: String? = null
)

@Serializable
data class AuthenticationResult(
    @SerialName("AccessToken") val accessToken: String,
    @SerialName("IdToken") val idToken: String,
    @SerialName("RefreshToken") val refreshToken: String? = null,
    @SerialName("ExpiresIn") val expiresIn: Long = 3600,
    @SerialName("TokenType") val tokenType: String? = null
)

@Serializable
data class ForgotPasswordRequest(
    @SerialName("ClientId") val clientId: String,
    @SerialName("Username") val username: String
)

@Serializable
data class ConfirmForgotPasswordRequest(
    @SerialName("ClientId") val clientId: String,
    @SerialName("Username") val username: String,
    @SerialName("ConfirmationCode") val confirmationCode: String,
    @SerialName("Password") val password: String
)

@Serializable
data class RevokeTokenRequest(
    @SerialName("ClientId") val clientId: String,
    @SerialName("Token") val token: String
)

@Serializable
class EmptyCognitoResponse

@Serializable
data class CognitoErrorBody(
    @SerialName("__type") val type: String? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("Message") val messageUpper: String? = null
)

/*
 * Hosted UI /oauth2/token response (snake_case).
 */
@Serializable
data class OAuthTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("id_token") val idToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long = 3600,
    @SerialName("token_type") val tokenType: String? = null
)

@Serializable
data class OAuthErrorBody(
    @SerialName("error") val error: String? = null,
    @SerialName("error_description") val errorDescription: String? = null
)
