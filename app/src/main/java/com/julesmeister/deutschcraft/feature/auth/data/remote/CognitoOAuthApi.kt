package com.julesmeister.deutschcraft.feature.auth.data.remote

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/*
 * Cognito Hosted UI token endpoint. Used only to exchange the
 * Google sign-in authorization code (with PKCE) for tokens.
 */
interface CognitoOAuthApi {

    @FormUrlEncoded
    @POST("oauth2/token")
    suspend fun exchangeAuthorizationCode(
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("client_id") clientId: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("code_verifier") codeVerifier: String
    ): OAuthTokenResponse
}
