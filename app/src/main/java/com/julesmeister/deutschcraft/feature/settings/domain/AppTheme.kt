package com.julesmeister.deutschcraft.feature.settings.domain

enum class AppTheme(
    val displayName: String
) {
    DARK(
        displayName = "Dark"
    ),

    LIGHT(
        displayName = "Light"
    );

    companion object {

        fun fromStoredName(
            value: String?
        ): AppTheme {
            return entries.firstOrNull { theme ->
                theme.name == value
            } ?: DARK
        }
    }
}