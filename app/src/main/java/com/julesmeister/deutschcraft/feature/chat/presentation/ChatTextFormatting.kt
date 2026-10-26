package com.julesmeister.deutschcraft.feature.chat.presentation

internal fun String.capitalizeFirstCharacter():
        String {
    val firstVisibleCharacterIndex =
        indexOfFirst { character ->
            !character.isWhitespace()
        }

    if (firstVisibleCharacterIndex == -1) {
        return this
    }

    val firstCharacter =
        this[firstVisibleCharacterIndex]

    if (firstCharacter.isUpperCase()) {
        return this
    }

    return replaceRange(
        range =
            firstVisibleCharacterIndex..
                    firstVisibleCharacterIndex,
        replacement =
            firstCharacter.uppercase()
    )
}