package com.julesmeister.deutschcraft.feature.settings.domain

enum class FontScaleOption(
    val displayName: String,
    val scale: Float
) {
    SMALL(
        displayName =
            "Small",
        scale =
            0.90f
    ),

    NORMAL(
        displayName =
            "Normal",
        scale =
            1.00f
    ),

    LARGE(
        displayName =
            "Large",
        scale =
            1.15f
    ),

    EXTRA_LARGE(
        displayName =
            "Extra large",
        scale =
            1.30f
    );

    companion object {
        fun fromStoredName(
            value: String?
        ): FontScaleOption {
            return entries.firstOrNull { option ->
                option.name == value
            } ?: NORMAL
        }
    }
}