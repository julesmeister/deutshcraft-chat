package com.julesmeister.deutschcraft.feature.threads.domain

import javax.inject.Inject

class LocalThreadTitleGenerator @Inject constructor() {

    fun generate(message: String): String {
        val cleanedMessage = cleanMessage(message)

        if (cleanedMessage.isBlank()) {
            return DEFAULT_TITLE
        }

        val titleWords = cleanedMessage
            .split(WHITESPACE_REGEX)
            .filter { word -> word.isNotBlank() }
            .take(MAX_TITLE_WORDS)

        val generatedTitle = titleWords
            .joinToString(separator = " ")
            .trim()
            .trimEnd('.', ',', '!', '?', ':', ';', '-', '—')

        if (generatedTitle.isBlank()) {
            return DEFAULT_TITLE
        }

        return generatedTitle
            .replaceFirstChar { firstCharacter ->
                if (firstCharacter.isLowerCase()) {
                    firstCharacter.titlecase()
                } else {
                    firstCharacter.toString()
                }
            }
            .take(MAX_TITLE_CHARACTERS)
            .trim()
    }

    private fun cleanMessage(message: String): String {
        return message
            .trim()
            .replace(MULTIPLE_LINES_REGEX, " ")
            .replace(WHITESPACE_REGEX, " ")
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .trim()
    }

    private companion object {
        const val DEFAULT_TITLE = "New conversation"
        const val MAX_TITLE_WORDS = 7
        const val MAX_TITLE_CHARACTERS = 48

        val WHITESPACE_REGEX = Regex("\\s+")
        val MULTIPLE_LINES_REGEX = Regex("[\\r\\n]+")
    }
}