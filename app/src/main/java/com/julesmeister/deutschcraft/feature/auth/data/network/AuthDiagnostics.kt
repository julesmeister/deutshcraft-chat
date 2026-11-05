package com.julesmeister.deutschcraft.feature.auth.data.network

import android.util.Log
import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.feature.auth.data.crypto.JwtDecoder
import okhttp3.Response

internal object AuthDiagnostics {
    private const val Tag = "DeutschCraftAuth"

    fun event(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(Tag, message)
        }
    }

    fun tokenSummary(token: String?): String {
        if (token == null) return "tokenPresent=false"
        val claims = JwtDecoder.decode(token)
            ?: return "tokenPresent=true jwtReadable=false"
        val expectedIssuer =
            "https://cognito-idp.${BuildConfig.COGNITO_REGION}.amazonaws.com/${BuildConfig.COGNITO_USER_POOL_ID}"
        val tokenUse = claims.tokenUse?.takeIf { it == "access" || it == "id" } ?: "unknown"
        return "tokenPresent=true jwtReadable=true tokenUse=$tokenUse " +
            "expiresInSeconds=${claims.expiresAtEpochSeconds?.minus(System.currentTimeMillis() / 1000)} " +
            "issuerMatchesConfig=${claims.issuer == expectedIssuer} " +
            "clientMatchesConfig=${claims.clientId == BuildConfig.COGNITO_CLIENT_ID} " +
            "audienceMatchesConfig=${claims.audience == BuildConfig.COGNITO_CLIENT_ID}"
    }

    fun response(stage: String, response: Response) {
        if (!BuildConfig.DEBUG) return
        fun safeHeader(name: String): String =
            response.header(name)
                ?.takeIf { it.length <= 160 && it.matches(Regex("[A-Za-z0-9._:/=-]+")) }
                ?: "absent"

        event(
            "$stage method=${response.request.method} host=${response.request.url.host} " +
                "status=${response.code} " +
                "awsRequestId=${safeHeader("x-amzn-requestid")} " +
                "gatewayRequestId=${safeHeader("apigw-requestid")} " +
                "awsErrorType=${safeHeader("x-amzn-errortype")}"
        )
    }
}
