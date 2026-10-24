package com.julesmeister.deutschcraft.feature.threads.data

import com.julesmeister.deutschcraft.feature.threads.domain.ConversationTitleGenerator
import javax.inject.Inject

class LocalConversationTitleGenerator @Inject constructor() :
    ConversationTitleGenerator {

    override fun generateTitle(
        messages: List<String>
    ): String {
        val meaningfulMessage = messages
            .asSequence()
            .map(String::trim)
            .firstOrNull { message ->
                message.length >= MINIMUM_MESSAGE_LENGTH
            }
            ?: return DEFAULT_TITLE

        return meaningfulMessage
            .replace(
                regex = TRAILING_PUNCTUATION,
                replacement = ""
            )
            .take(MAXIMUM_TITLE_LENGTH)
            .trim()
            .ifBlank {
                DEFAULT_TITLE
            }
    }

    private companion object {
        const val DEFAULT_TITLE =
            "Neue Unterhaltung"

        const val MINIMUM_MESSAGE_LENGTH = 4

        const val MAXIMUM_TITLE_LENGTH = 36

        val TRAILING_PUNCTUATION =
            Regex("[.!?]+$")
    }
}