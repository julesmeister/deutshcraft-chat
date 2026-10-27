package com.julesmeister.deutschcraft.feature.settings.domain

enum class ProgressLanguage(
    val displayName: String
) {
    GERMAN(
        displayName = "Deutsch"
    ),

    ENGLISH(
        displayName = "English"
    );

    companion object {

        fun fromStoredName(
            value: String?
        ): ProgressLanguage {
            return entries.firstOrNull { language ->
                language.name == value
            } ?: GERMAN
        }
    }
}