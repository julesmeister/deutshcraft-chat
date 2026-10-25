package com.julesmeister.deutschcraft.feature.chat.data.correction

import android.content.Context
import androidx.core.content.edit
import com.julesmeister.deutschcraft.feature.chat.domain.MessageCorrection
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageCorrectionStore @Inject constructor(
    @ApplicationContext
    context: Context
) {

    private val preferences =
        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    fun save(
        correction: MessageCorrection
    ) {
        val correctedSentence =
            correction.correctedSentence.trim()

        if (correctedSentence.isBlank()) {
            return
        }

        preferences.edit {
            putString(
                correctionKey(
                    messageId = correction.messageId
                ),
                correctedSentence
            )
        }
    }

    fun getCorrection(
        messageId: String
    ): String? {
        return preferences
            .getString(
                correctionKey(
                    messageId = messageId
                ),
                null
            )
            ?.trim()
            ?.takeIf { correction ->
                correction.isNotBlank()
            }
    }

    fun getCorrections(
        messageIds: Collection<String>
    ): Map<String, String> {
        if (messageIds.isEmpty()) {
            return emptyMap()
        }

        return buildMap {
            messageIds.forEach { messageId ->
                val correction =
                    getCorrection(
                        messageId = messageId
                    )

                if (correction != null) {
                    put(
                        messageId,
                        correction
                    )
                }
            }
        }
    }

    fun delete(
        messageId: String
    ) {
        preferences.edit {
            remove(
                correctionKey(
                    messageId = messageId
                )
            )
        }
    }

    fun clear() {
        preferences.edit {
            clear()
        }
    }

    private fun correctionKey(
        messageId: String
    ): String {
        return "$CORRECTION_KEY_PREFIX$messageId"
    }

    private companion object {
        const val PREFERENCES_NAME =
            "deutschcraft_message_corrections"

        const val CORRECTION_KEY_PREFIX =
            "message_correction_"
    }
}