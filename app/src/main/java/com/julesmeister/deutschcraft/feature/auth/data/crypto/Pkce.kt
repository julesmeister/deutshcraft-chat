package com.julesmeister.deutschcraft.feature.auth.data.crypto

import java.security.MessageDigest
import java.security.SecureRandom

/*
 * Proof Key for Code Exchange (RFC 7636), S256 method.
 */
internal data class PkcePair(
    val verifier: String,
    val challenge: String
)

internal object Pkce {

    private const val VerifierByteCount = 32
    private const val StateByteCount = 16

    private val secureRandom =
        SecureRandom()

    fun generate(): PkcePair {
        val verifier =
            randomUrlSafeString(VerifierByteCount)

        return PkcePair(
            verifier = verifier,
            challenge = challengeFor(verifier)
        )
    }

    fun challengeFor(
        verifier: String
    ): String {
        val digest =
            MessageDigest
                .getInstance("SHA-256")
                .digest(verifier.toByteArray(Charsets.US_ASCII))

        return Base64Url.encode(digest)
    }

    fun generateState(): String =
        randomUrlSafeString(StateByteCount)

    private fun randomUrlSafeString(
        byteCount: Int
    ): String {
        val bytes =
            ByteArray(byteCount)

        secureRandom.nextBytes(bytes)

        return Base64Url.encode(bytes)
    }
}
