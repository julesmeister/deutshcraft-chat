package com.julesmeister.deutschcraft.feature.auth.data.remote

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/*
 * Cognito user-pool API, called without Amplify.
 *
 * Every operation is a POST to the regional root with the
 * operation named in X-Amz-Target. The app client has no secret,
 * so no SECRET_HASH is needed.
 */
interface CognitoAuthApi {

    @Headers("X-Amz-Target: $TargetPrefix.SignUp")
    @POST("/")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): SignUpResponse

    @Headers("X-Amz-Target: $TargetPrefix.ConfirmSignUp")
    @POST("/")
    suspend fun confirmSignUp(
        @Body request: ConfirmSignUpRequest
    ): EmptyCognitoResponse

    @Headers("X-Amz-Target: $TargetPrefix.ResendConfirmationCode")
    @POST("/")
    suspend fun resendConfirmationCode(
        @Body request: ResendConfirmationCodeRequest
    ): CodeDeliveryResponse

    @Headers("X-Amz-Target: $TargetPrefix.InitiateAuth")
    @POST("/")
    suspend fun initiateAuth(
        @Body request: InitiateAuthRequest
    ): InitiateAuthResponse

    @Headers("X-Amz-Target: $TargetPrefix.ForgotPassword")
    @POST("/")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): CodeDeliveryResponse

    @Headers("X-Amz-Target: $TargetPrefix.ConfirmForgotPassword")
    @POST("/")
    suspend fun confirmForgotPassword(
        @Body request: ConfirmForgotPasswordRequest
    ): EmptyCognitoResponse

    @Headers("X-Amz-Target: $TargetPrefix.RevokeToken")
    @POST("/")
    suspend fun revokeToken(
        @Body request: RevokeTokenRequest
    ): EmptyCognitoResponse

    companion object {

        const val TargetPrefix =
            "AWSCognitoIdentityProviderService"

        const val ContentType =
            "application/x-amz-json-1.1"

        const val AuthFlowUserPassword =
            "USER_PASSWORD_AUTH"

        const val AuthFlowRefreshToken =
            "REFRESH_TOKEN_AUTH"
    }
}
