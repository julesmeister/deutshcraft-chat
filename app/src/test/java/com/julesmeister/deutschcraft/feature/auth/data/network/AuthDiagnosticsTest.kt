package com.julesmeister.deutschcraft.feature.auth.data.network

import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.feature.auth.data.crypto.Base64Url
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthDiagnosticsTest {
    @Test
    fun summaryReportsConfigurationAndExpiryWithoutLeakingIdentityOrToken() {
        val issuer =
            "https://cognito-idp.${BuildConfig.COGNITO_REGION}.amazonaws.com/${BuildConfig.COGNITO_USER_POOL_ID}"
        val payload = """
            {"sub":"private-subject","email":"private@example.com","name":"Private Name",
             "iss":"$issuer","client_id":"${BuildConfig.COGNITO_CLIENT_ID}",
             "token_use":"access","exp":1}
        """.trimIndent()
        val token = "e30.${Base64Url.encode(payload.toByteArray())}.private-signature"
        val summary = AuthDiagnostics.tokenSummary(token)

        assertTrue(summary.contains("tokenPresent=true jwtReadable=true tokenUse=access"))
        assertTrue(summary.contains("issuerMatchesConfig=true"))
        assertTrue(summary.contains("clientMatchesConfig=true"))
        assertTrue(summary.contains("expiresInSeconds=-"))
        for (privateValue in listOf(token, "private-subject", "private@example.com", "Private Name")) {
            assertFalse(summary.contains(privateValue))
        }
    }

    @Test
    fun missingAndMalformedTokensAreDescribedWithoutEchoingThem() {
        assertEquals("tokenPresent=false", AuthDiagnostics.tokenSummary(null))
        assertEquals(
            "tokenPresent=true jwtReadable=false",
            AuthDiagnostics.tokenSummary("private-invalid-token")
        )
    }
}
