package com.julesmeister.deutschcraft.feature.auth.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.julesmeister.deutschcraft.feature.auth.data.crypto.Base64Url
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/*
 * AES-256-GCM with a non-exportable key held in the Android
 * Keystore. Output is base64url(iv || ciphertext).
 */
@Singleton
class KeystoreCipher
@Inject constructor() {

    fun encrypt(
        plainText: String
    ): String {
        val cipher =
            Cipher.getInstance(Transformation)

        cipher.init(
            Cipher.ENCRYPT_MODE,
            secretKey()
        )

        val cipherText =
            cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        return Base64Url.encode(cipher.iv + cipherText)
    }

    fun decrypt(
        encoded: String
    ): String? =
        runCatching {
            val bytes =
                Base64Url.decode(encoded)

            val iv =
                bytes.copyOfRange(0, IvSizeBytes)

            val cipherText =
                bytes.copyOfRange(IvSizeBytes, bytes.size)

            val cipher =
                Cipher.getInstance(Transformation)

            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey(),
                GCMParameterSpec(TagSizeBits, iv)
            )

            cipher
                .doFinal(cipherText)
                .toString(Charsets.UTF_8)
        }.getOrNull()

    private fun secretKey(): SecretKey {
        val keyStore =
            KeyStore
                .getInstance(KeyStoreProvider)
                .apply { load(null) }

        (keyStore.getEntry(KeyAlias, null) as? KeyStore.SecretKeyEntry)
            ?.let { entry ->
                return entry.secretKey
            }

        val generator =
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KeyStoreProvider
            )

        generator.init(
            KeyGenParameterSpec
                .Builder(
                    KeyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KeySizeBits)
                .build()
        )

        return generator.generateKey()
    }

    private companion object {

        const val KeyStoreProvider = "AndroidKeyStore"
        const val KeyAlias = "deutschcraft_auth_tokens"
        const val Transformation = "AES/GCM/NoPadding"
        const val KeySizeBits = 256
        const val IvSizeBytes = 12
        const val TagSizeBits = 128
    }
}
