package com.julesmeister.deutschcraft.feature.auth

import com.julesmeister.deutschcraft.feature.auth.data.crypto.Base64Url
import com.julesmeister.deutschcraft.feature.auth.data.crypto.JwtDecoder
import com.julesmeister.deutschcraft.feature.auth.data.crypto.Pkce
import com.julesmeister.deutschcraft.feature.auth.data.remote.CognitoErrorMapper
import com.julesmeister.deutschcraft.feature.auth.domain.AuthError
import com.julesmeister.deutschcraft.feature.auth.domain.AuthProvider
import com.julesmeister.deutschcraft.feature.auth.domain.AuthSession
import com.julesmeister.deutschcraft.feature.auth.domain.GuestMessagePolicy
import java.io.IOException
import kotlin.random.Random
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthCryptoTest {

    @Test
    fun base64Url_roundTripsEveryLength() {
        val random =
            Random(42)

        for (size in 0..64) {
            val bytes =
                random.nextBytes(size)

            val encoded =
                Base64Url.encode(bytes)

            assertFalse(encoded.contains('='))
            assertFalse(encoded.contains('+'))
            assertFalse(encoded.contains('/'))
            assertArrayEquals(bytes, Base64Url.decode(encoded))
        }
    }

    @Test
    fun base64Url_matchesRfc4648Vectors() {
        assertEquals("", Base64Url.encode("".toByteArray()))
        assertEquals("Zg", Base64Url.encode("f".toByteArray()))
        assertEquals("Zm8", Base64Url.encode("fo".toByteArray()))
        assertEquals("Zm9v", Base64Url.encode("foo".toByteArray()))
        assertEquals("Zm9vYmFy", Base64Url.encode("foobar".toByteArray()))
    }

    @Test
    fun base64Url_decodesPaddedStandardAlphabet() {
        assertArrayEquals(
            byteArrayOf(0xFB.toByte(), 0xFF.toByte()),
            Base64Url.decode("+/8=")
        )
    }

    @Test
    fun pkce_challengeIsBase64UrlSha256OfVerifier() {
        // Expected value computed independently with Python's hashlib + urlsafe_b64encode.
        assertEquals(
            "HuAT-KMyvOVt4y17pQFEga0rQpMDwJ9IRyQVjuiYQ-o",
            Pkce.challengeFor("dBjftJeZ4CVP-mJ92K9xqUKX7BgUIbEN9ByCKWr9IsI")
        )
    }

    @Test
    fun pkce_generatesValidUniquePairs() {
        val first =
            Pkce.generate()

        val second =
            Pkce.generate()

        assertNotEquals(first.verifier, second.verifier)
        assertTrue(first.verifier.length in 43..128)
        assertEquals(Pkce.challengeFor(first.verifier), first.challenge)
        assertNotEquals(Pkce.generateState(), Pkce.generateState())
    }

    @Test
    fun jwtDecoder_readsCognitoClaims() {
        val payload =
            """
            {"sub":"abc-123","email":"anna@example.com","name":"Anna",
             "iss":"https://cognito.example/pool","client_id":"test-client",
             "aud":"test-audience","token_use":"access",
             "exp":1700000000,
             "identities":[{"providerName":"Google","userId":"1"}]}
            """.trimIndent()

        val token =
            "${Base64Url.encode("{}".toByteArray())}." +
                    "${Base64Url.encode(payload.toByteArray())}.sig"

        val claims =
            JwtDecoder.decode(token)!!

        assertEquals("abc-123", claims.subject)
        assertEquals("anna@example.com", claims.email)
        assertEquals("Anna", claims.name)
        assertEquals(1_700_000_000L, claims.expiresAtEpochSeconds)
        assertEquals("Google", claims.identityProviderName)
        assertEquals("https://cognito.example/pool", claims.issuer)
        assertEquals("test-client", claims.clientId)
        assertEquals("test-audience", claims.audience)
        assertEquals("access", claims.tokenUse)
    }

    @Test
    fun jwtDecoder_rejectsMalformedTokens() {
        assertNull(JwtDecoder.decode("not-a-jwt"))
        assertNull(JwtDecoder.decode("a.!!!.c"))
    }
}

class GuestMessagePolicyTest {

    private val signedIn =
        AuthSession.SignedIn(
            email = "anna@example.com",
            displayName = null,
            provider = AuthProvider.EMAIL
        )

    @Test
    fun guest_countsDownToLimit() {
        assertEquals(5, GuestMessagePolicy.remainingMessages(AuthSession.Guest, 0))
        assertEquals(1, GuestMessagePolicy.remainingMessages(AuthSession.Guest, 4))
        assertEquals(0, GuestMessagePolicy.remainingMessages(AuthSession.Guest, 5))
        assertEquals(0, GuestMessagePolicy.remainingMessages(AuthSession.Guest, 9))
        assertTrue(GuestMessagePolicy.isLimitReached(AuthSession.Guest, 5))
        assertFalse(GuestMessagePolicy.isLimitReached(AuthSession.Guest, 4))
    }

    @Test
    fun signedInAndLoading_haveNoLimit() {
        assertNull(GuestMessagePolicy.remainingMessages(signedIn, 50))
        assertNull(GuestMessagePolicy.remainingMessages(AuthSession.Loading, 50))
        assertFalse(GuestMessagePolicy.isLimitReached(signedIn, 50))
    }
}

class CognitoErrorMapperTest {

    @Test
    fun mapsCognitoExceptionTypes() {
        assertEquals(
            AuthError.InvalidCredentials,
            CognitoErrorMapper.mapCognitoType("NotAuthorizedException", "Incorrect username or password.")
        )
        assertEquals(
            AuthError.SessionExpired,
            CognitoErrorMapper.mapCognitoType("NotAuthorizedException", "Invalid Refresh Token")
        )
        assertEquals(
            AuthError.UserNotConfirmed,
            CognitoErrorMapper.mapCognitoType(
                "com.amazonaws.cognito.identity.idp.model#UserNotConfirmedException",
                null
            )
        )
        assertEquals(
            AuthError.UserAlreadyExists,
            CognitoErrorMapper.mapCognitoType("UsernameExistsException", null)
        )
        assertEquals(
            AuthError.InvalidCode,
            CognitoErrorMapper.mapCognitoType("CodeMismatchException", null)
        )
        assertEquals(
            AuthError.InvalidPassword("too short"),
            CognitoErrorMapper.mapCognitoType("InvalidPasswordException", "too short")
        )
        assertEquals(
            AuthError.Unknown("odd"),
            CognitoErrorMapper.mapCognitoType("SomethingNew", "odd")
        )
    }

    @Test
    fun mapsIoFailuresToNetwork() {
        assertEquals(
            AuthError.Network,
            CognitoErrorMapper.map(IOException("offline"))
        )
    }
}
