package com.julesmeister.deutschcraft.feature.auth.data.crypto

/*
 * URL-safe Base64 without padding (RFC 4648 §5).
 *
 * java.util.Base64 needs API 26 and android.util.Base64 is not
 * available in JVM unit tests, so PKCE and JWT parsing use this.
 */
internal object Base64Url {

    private const val Alphabet =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

    private val decodeTable =
        IntArray(128) { -1 }.also { table ->
            Alphabet.forEachIndexed { index, char ->
                table[char.code] = index
            }
            // Accept standard Base64 characters too.
            table['+'.code] = 62
            table['/'.code] = 63
        }

    fun encode(
        bytes: ByteArray
    ): String {
        val output =
            StringBuilder((bytes.size * 4 + 2) / 3)

        var index = 0

        while (index + 2 < bytes.size) {
            val chunk =
                (bytes[index].toInt() and 0xFF shl 16) or
                        (bytes[index + 1].toInt() and 0xFF shl 8) or
                        (bytes[index + 2].toInt() and 0xFF)

            output.append(Alphabet[chunk shr 18 and 0x3F])
            output.append(Alphabet[chunk shr 12 and 0x3F])
            output.append(Alphabet[chunk shr 6 and 0x3F])
            output.append(Alphabet[chunk and 0x3F])

            index += 3
        }

        when (bytes.size - index) {
            1 -> {
                val chunk =
                    bytes[index].toInt() and 0xFF shl 16

                output.append(Alphabet[chunk shr 18 and 0x3F])
                output.append(Alphabet[chunk shr 12 and 0x3F])
            }

            2 -> {
                val chunk =
                    (bytes[index].toInt() and 0xFF shl 16) or
                            (bytes[index + 1].toInt() and 0xFF shl 8)

                output.append(Alphabet[chunk shr 18 and 0x3F])
                output.append(Alphabet[chunk shr 12 and 0x3F])
                output.append(Alphabet[chunk shr 6 and 0x3F])
            }
        }

        return output.toString()
    }

    fun decode(
        text: String
    ): ByteArray {
        val clean =
            text.trimEnd('=')

        val output =
            ByteArray(clean.length * 3 / 4)

        var buffer = 0
        var bits = 0
        var position = 0

        for (char in clean) {
            val value =
                if (char.code < decodeTable.size) {
                    decodeTable[char.code]
                } else {
                    -1
                }

            require(value >= 0) {
                "Invalid Base64 character '$char'"
            }

            buffer = (buffer shl 6) or value
            bits += 6

            if (bits >= 8) {
                bits -= 8
                output[position++] =
                    (buffer shr bits and 0xFF).toByte()
            }
        }

        return if (position == output.size) {
            output
        } else {
            output.copyOf(position)
        }
    }
}
