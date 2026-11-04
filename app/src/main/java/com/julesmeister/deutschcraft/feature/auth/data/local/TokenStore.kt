package com.julesmeister.deutschcraft.feature.auth.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.authDataStore by preferencesDataStore(
    name = "deutschcraft_auth"
)

@Serializable
data class StoredTokens(
    val accessToken: String,
    val idToken: String,
    val refreshToken: String,
    val expiresAtEpochMillis: Long,
    val provider: String
)

/*
 * PKCE verifier + state for an in-flight Google sign-in. Kept on
 * disk so the flow survives the app being killed while the
 * Custom Tab is open.
 */
@Serializable
data class PendingOAuth(
    val codeVerifier: String,
    val state: String,
    val createdAtEpochMillis: Long
)

/*
 * Tokens are encrypted with a Keystore key before they reach
 * DataStore, so a backup or a rooted read of the prefs file
 * does not expose them.
 */
@Singleton
class TokenStore
@Inject constructor(
    @ApplicationContext
    private val context: Context,

    private val cipher: KeystoreCipher
) {

    private val json =
        Json { ignoreUnknownKeys = true }

    suspend fun readTokens(): StoredTokens? =
        readEncrypted(TokensKey)
            ?.let { plain ->
                runCatching {
                    json.decodeFromString(StoredTokens.serializer(), plain)
                }.getOrNull()
            }

    suspend fun writeTokens(
        tokens: StoredTokens
    ) {
        writeEncrypted(
            key = TokensKey,
            plainText = json.encodeToString(StoredTokens.serializer(), tokens)
        )
    }

    suspend fun clearTokens() {
        context.authDataStore.edit { preferences ->
            preferences.remove(TokensKey)
        }
    }

    suspend fun readPendingOAuth(): PendingOAuth? =
        readEncrypted(PendingOAuthKey)
            ?.let { plain ->
                runCatching {
                    json.decodeFromString(PendingOAuth.serializer(), plain)
                }.getOrNull()
            }

    suspend fun writePendingOAuth(
        pending: PendingOAuth
    ) {
        writeEncrypted(
            key = PendingOAuthKey,
            plainText = json.encodeToString(PendingOAuth.serializer(), pending)
        )
    }

    suspend fun clearPendingOAuth() {
        context.authDataStore.edit { preferences ->
            preferences.remove(PendingOAuthKey)
        }
    }

    private suspend fun readEncrypted(
        key: androidx.datastore.preferences.core.Preferences.Key<String>
    ): String? {
        val encoded =
            context
                .authDataStore
                .data
                .first()[key]
                ?: return null

        return cipher.decrypt(encoded)
    }

    private suspend fun writeEncrypted(
        key: androidx.datastore.preferences.core.Preferences.Key<String>,
        plainText: String
    ) {
        val encoded =
            cipher.encrypt(plainText)

        context.authDataStore.edit { preferences ->
            preferences[key] = encoded
        }
    }

    private companion object {

        val TokensKey =
            stringPreferencesKey("tokens")

        val PendingOAuthKey =
            stringPreferencesKey("pending_oauth")
    }
}
