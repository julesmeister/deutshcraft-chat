package com.julesmeister.deutschcraft.feature.auth.data.crypto

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull

/*
 * Reads (does not verify) the claims of a Cognito JWT. The
 * signature is verified by API Gateway; the app only needs the
 * display email and the expiry time.
 */
internal data class JwtClaims(
    val subject: String?,
    val email: String?,
    val name: String?,
    val expiresAtEpochSeconds: Long?,
    val identityProviderName: String?,
    val issuer: String?,
    val clientId: String?,
    val audience: String?,
    val tokenUse: String?
)

internal object JwtDecoder {

    private val json =
        Json { ignoreUnknownKeys = true }

    fun decode(
        token: String
    ): JwtClaims? {
        val parts =
            token.split('.')

        if (parts.size != 3) {
            return null
        }

        val payload =
            runCatching {
                json
                    .parseToJsonElement(
                        Base64Url
                            .decode(parts[1])
                            .toString(Charsets.UTF_8)
                    )
                    .jsonObject
            }.getOrNull()
                ?: return null

        return JwtClaims(
            subject = payload.string("sub"),
            email = payload.string("email"),
            name = payload.string("name"),
            expiresAtEpochSeconds =
                (payload["exp"] as? JsonPrimitive)?.longOrNull,
            identityProviderName = payload.identityProviderName(),
            issuer = payload.string("iss"),
            clientId = payload.string("client_id"),
            audience = payload.string("aud"),
            tokenUse = payload.string("token_use")
        )
    }

    private fun JsonObject.string(
        key: String
    ): String? =
        (this[key] as? JsonPrimitive)
            ?.takeIf { it.isString }
            ?.content

    /*
     * Federated ID tokens carry
     * "identities": [{ "providerName": "Google", ... }].
     */
    private fun JsonObject.identityProviderName(): String? =
        runCatching {
            val identities =
                this["identities"] as? kotlinx.serialization.json.JsonArray
                    ?: return null

            (identities.firstOrNull() as? JsonObject)
                ?.string("providerName")
        }.getOrNull()
}
